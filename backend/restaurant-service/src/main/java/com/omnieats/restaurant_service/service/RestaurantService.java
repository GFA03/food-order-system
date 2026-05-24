package com.omnieats.restaurant_service.service;

import com.omnieats.restaurant_service.dto.RestaurantSummaryDto;
import com.omnieats.restaurant_service.exception.BadRequestException;
import com.omnieats.restaurant_service.exception.RestaurantNotFoundException;
import com.omnieats.restaurant_service.model.CuisineTag;
import com.omnieats.restaurant_service.model.Restaurant;
import com.omnieats.restaurant_service.repository.CuisineTagRepository;
import com.omnieats.restaurant_service.repository.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    private static final Logger log = LoggerFactory.getLogger(RestaurantService.class);

    private final RestaurantRepository restaurantRepository;
    private final CuisineTagRepository cuisineTagRepository;

    public RestaurantService(RestaurantRepository restaurantRepository, CuisineTagRepository cuisineTagRepository) {
        this.restaurantRepository = restaurantRepository;
        this.cuisineTagRepository = cuisineTagRepository;
    }

    public Page<Restaurant> getRestaurants(List<UUID> tags, Pageable pageable) {
        log.debug("Fetching restaurants: tags={}, page={}", tags, pageable.getPageNumber());
        if (tags != null && !tags.isEmpty()) {
            return restaurantRepository.findByCuisineTagsIdIn(tags, pageable);
        }
        return restaurantRepository.findAll(pageable);
    }

    /** Top-rated restaurants, cached in Redis (evicted on any restaurant write). */
    @Cacheable("topRatedRestaurants")
    public List<RestaurantSummaryDto> getTopRated() {
        log.debug("Fetching top-rated restaurants (cache miss)");
        return restaurantRepository.findTopRated(PageRequest.of(0, 10)).stream()
                .map(RestaurantSummaryDto::from)
                .toList();
    }

    public Restaurant getRestaurant(UUID id) {
        log.debug("Fetching restaurant: id={}", id);
        return restaurantRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Restaurant not found: id={}", id);
                    return new RestaurantNotFoundException("Restaurant not found: " + id);
                });
    }

    @CacheEvict(value = "topRatedRestaurants", allEntries = true)
    public Restaurant createRestaurant(String name, String description, Double rating, Integer deliveryTime, List<UUID> tagIds) {
        log.debug("Creating restaurant: name={}, tagIds={}", name, tagIds);
        List<CuisineTag> tags = getTagsByIds(tagIds);
        Restaurant restaurant = new Restaurant(name, description, rating, deliveryTime, tags);
        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Restaurant created: id={}, name={}", saved.getId(), name);
        return saved;
    }

    @CacheEvict(value = "topRatedRestaurants", allEntries = true)
    public Restaurant updateRestaurant(UUID id, String name, String description, Double rating, Integer deliveryTime, List<UUID> tagIds) {
        log.debug("Updating restaurant: id={}", id);
        Restaurant restaurant = getRestaurant(id);
        restaurant.setName(name);
        restaurant.setDescription(description);
        restaurant.setRating(rating);
        restaurant.setDeliveryTime(deliveryTime);
        if (tagIds != null) {
            restaurant.setCuisineTags(getTagsByIds(tagIds));
        }
        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Restaurant updated: id={}, name={}", id, name);
        return saved;
    }

    @CacheEvict(value = "topRatedRestaurants", allEntries = true)
    public void deleteRestaurant(UUID id) {
        if (!restaurantRepository.existsById(id)) {
            log.error("Delete failed — restaurant not found: id={}", id);
            throw new RestaurantNotFoundException("Restaurant not found: " + id);
        }
        restaurantRepository.deleteById(id);
        log.info("Restaurant deleted: id={}", id);
    }

    private List<CuisineTag> getTagsByIds(List<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        List<CuisineTag> foundTags = cuisineTagRepository.findAllById(tagIds);
        if (foundTags.size() != tagIds.size()) {
            throw new BadRequestException("One or more cuisine tags not found");
        }
        return foundTags;
    }
}

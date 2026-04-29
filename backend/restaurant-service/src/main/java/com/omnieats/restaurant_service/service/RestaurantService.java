package com.omnieats.restaurant_service.service;

import com.omnieats.restaurant_service.model.CuisineTag;
import com.omnieats.restaurant_service.model.Restaurant;
import com.omnieats.restaurant_service.repository.CuisineTagRepository;
import com.omnieats.restaurant_service.repository.RestaurantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final CuisineTagRepository cuisineTagRepository;

    public RestaurantService(RestaurantRepository restaurantRepository, CuisineTagRepository cuisineTagRepository) {
        this.restaurantRepository = restaurantRepository;
        this.cuisineTagRepository = cuisineTagRepository;
    }

    public Page<Restaurant> getRestaurants(List<UUID> tags, Pageable pageable) {
        if (tags != null && !tags.isEmpty()) {
            return restaurantRepository.findByCuisineTagsIdIn(tags, pageable);
        }
        return restaurantRepository.findAll(pageable);
    }

    public Restaurant getRestaurant(UUID id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));
    }

    public Restaurant createRestaurant(String name, String description, Double rating, Integer deliveryTime, List<UUID> tagIds) {
        List<CuisineTag> tags = getTagsByIds(tagIds);
        Restaurant restaurant = new Restaurant(name, description, rating, deliveryTime, tags);
        return restaurantRepository.save(restaurant);
    }

    public Restaurant updateRestaurant(UUID id, String name, String description, Double rating, Integer deliveryTime, List<UUID> tagIds) {
        Restaurant restaurant = getRestaurant(id);
        restaurant.setName(name);
        restaurant.setDescription(description);
        restaurant.setRating(rating);
        restaurant.setDeliveryTime(deliveryTime);
        if (tagIds != null) {
            restaurant.setCuisineTags(getTagsByIds(tagIds));
        }
        return restaurantRepository.save(restaurant);
    }

    public void deleteRestaurant(UUID id) {
        if (!restaurantRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found");
        }
        restaurantRepository.deleteById(id);
    }

    private List<CuisineTag> getTagsByIds(List<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        List<CuisineTag> foundTags = cuisineTagRepository.findAllById(tagIds);
        if (foundTags.size() != tagIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more cuisine tags not found");
        }
        return foundTags;
    }
}

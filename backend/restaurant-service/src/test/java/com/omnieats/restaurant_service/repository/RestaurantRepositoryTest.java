package com.omnieats.restaurant_service.repository;

import com.omnieats.restaurant_service.model.CuisineTag;
import com.omnieats.restaurant_service.model.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RestaurantRepositoryTest {

    @Autowired private RestaurantRepository restaurantRepository;
    @Autowired private CuisineTagRepository cuisineTagRepository;

    private CuisineTag italianTag;
    private CuisineTag veganTag;
    private Restaurant italianRestaurant;
    private Restaurant veganRestaurant;

    @BeforeEach
    void setUp() {
        italianTag = cuisineTagRepository.save(new CuisineTag("Italian"));
        veganTag = cuisineTagRepository.save(new CuisineTag("Vegan"));
        italianRestaurant = restaurantRepository.save(
                new Restaurant("Luigi's", "Italian food", 4.8, 30, List.of(italianTag)));
        veganRestaurant = restaurantRepository.save(
                new Restaurant("Green Leaf", "Vegan food", 4.5, 20, List.of(veganTag)));
    }

    @Test
    void findByCuisineTagsIdIn_MatchingTag_ReturnsRestaurant() {
        Page<Restaurant> result = restaurantRepository.findByCuisineTagsIdIn(
                List.of(italianTag.getId()), PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Luigi's", result.getContent().get(0).getName());
    }

    @Test
    void findByCuisineTagsIdIn_NoMatchingTag_ReturnsEmpty() {
        Page<Restaurant> result = restaurantRepository.findByCuisineTagsIdIn(
                List.of(UUID.randomUUID()), PageRequest.of(0, 10));

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void findByCuisineTagsIdIn_MultipleTagIds_ReturnsDistinctRestaurants() {
        Page<Restaurant> result = restaurantRepository.findByCuisineTagsIdIn(
                List.of(italianTag.getId(), veganTag.getId()), PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void findById_Exists_ReturnsRestaurant() {
        Optional<Restaurant> result = restaurantRepository.findById(italianRestaurant.getId());

        assertTrue(result.isPresent());
        assertEquals("Luigi's", result.get().getName());
    }

    @Test
    void findById_NotExists_ReturnsEmpty() {
        Optional<Restaurant> result = restaurantRepository.findById(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_ReturnsBothRestaurants() {
        Page<Restaurant> result = restaurantRepository.findAll(PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void existsById_True_WhenExists() {
        assertTrue(restaurantRepository.existsById(italianRestaurant.getId()));
    }

    @Test
    void existsById_False_WhenNotExists() {
        assertFalse(restaurantRepository.existsById(UUID.randomUUID()));
    }
}

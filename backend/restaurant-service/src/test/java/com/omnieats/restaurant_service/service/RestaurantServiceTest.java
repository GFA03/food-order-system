package com.omnieats.restaurant_service.service;

import com.omnieats.restaurant_service.model.CuisineTag;
import com.omnieats.restaurant_service.model.Restaurant;
import com.omnieats.restaurant_service.repository.CuisineTagRepository;
import com.omnieats.restaurant_service.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock private RestaurantRepository restaurantRepository;
    @Mock private CuisineTagRepository cuisineTagRepository;
    @InjectMocks private RestaurantService restaurantService;

    private UUID restaurantId;
    private UUID tagId;
    private CuisineTag tag;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID();
        tagId = UUID.randomUUID();
        tag = new CuisineTag("Italian");
        ReflectionTestUtils.setField(tag, "id", tagId);
        restaurant = new Restaurant("Pizza Place", "Italian food", 4.5, 30, List.of(tag));
        ReflectionTestUtils.setField(restaurant, "id", restaurantId);
    }

    @Test
    void getRestaurants_NoTags_ReturnsAll() {
        Page<Restaurant> page = new PageImpl<>(List.of(restaurant));
        when(restaurantRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Restaurant> result = restaurantService.getRestaurants(null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        verify(restaurantRepository).findAll(any(Pageable.class));
        verify(restaurantRepository, never()).findByCuisineTagsIdIn(any(), any());
    }

    @Test
    void getRestaurants_EmptyTagList_ReturnsAll() {
        Page<Restaurant> page = new PageImpl<>(List.of(restaurant));
        when(restaurantRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Restaurant> result = restaurantService.getRestaurants(List.of(), PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getRestaurants_WithTags_FiltersResults() {
        Page<Restaurant> page = new PageImpl<>(List.of(restaurant));
        when(restaurantRepository.findByCuisineTagsIdIn(List.of(tagId), PageRequest.of(0, 10))).thenReturn(page);

        Page<Restaurant> result = restaurantService.getRestaurants(List.of(tagId), PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        verify(restaurantRepository).findByCuisineTagsIdIn(List.of(tagId), PageRequest.of(0, 10));
    }

    @Test
    void getRestaurant_Found_ReturnsRestaurant() {
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        Restaurant result = restaurantService.getRestaurant(restaurantId);

        assertNotNull(result);
        assertEquals("Pizza Place", result.getName());
    }

    @Test
    void getRestaurant_NotFound_Throws404() {
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> restaurantService.getRestaurant(restaurantId));
    }

    @Test
    void createRestaurant_WithTags_SavesAndReturns() {
        when(cuisineTagRepository.findAllById(List.of(tagId))).thenReturn(List.of(tag));
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(i -> i.getArgument(0));

        Restaurant result = restaurantService.createRestaurant("Pizza Place", "Italian", 4.5, 30, List.of(tagId));

        assertEquals("Pizza Place", result.getName());
        assertEquals(1, result.getCuisineTags().size());
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    void createRestaurant_MissingTags_Throws400() {
        when(cuisineTagRepository.findAllById(List.of(tagId))).thenReturn(List.of());

        assertThrows(ResponseStatusException.class,
                () -> restaurantService.createRestaurant("Pizza Place", "Italian", 4.5, 30, List.of(tagId)));
    }

    @Test
    void createRestaurant_NoTags_SavesWithoutTags() {
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(i -> i.getArgument(0));

        Restaurant result = restaurantService.createRestaurant("Pizza Place", "Italian", 4.5, 30, null);

        assertEquals(0, result.getCuisineTags().size());
    }

    @Test
    void updateRestaurant_Success_UpdatesFields() {
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(i -> i.getArgument(0));

        Restaurant result = restaurantService.updateRestaurant(restaurantId, "New Name", "New Desc", 4.0, 25, null);

        assertEquals("New Name", result.getName());
        assertEquals("New Desc", result.getDescription());
        assertEquals(4.0, result.getRating());
        assertEquals(25, result.getDeliveryTime());
    }

    @Test
    void deleteRestaurant_Exists_Deletes() {
        when(restaurantRepository.existsById(restaurantId)).thenReturn(true);

        restaurantService.deleteRestaurant(restaurantId);

        verify(restaurantRepository).deleteById(restaurantId);
    }

    @Test
    void deleteRestaurant_NotFound_Throws404() {
        when(restaurantRepository.existsById(restaurantId)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> restaurantService.deleteRestaurant(restaurantId));
        verify(restaurantRepository, never()).deleteById(any());
    }
}

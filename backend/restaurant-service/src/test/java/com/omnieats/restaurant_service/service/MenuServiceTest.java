package com.omnieats.restaurant_service.service;

import com.omnieats.restaurant_service.model.MenuItem;
import com.omnieats.restaurant_service.model.Restaurant;
import com.omnieats.restaurant_service.repository.MenuItemRepository;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MenuServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private RestaurantService restaurantService;

    @InjectMocks
    private MenuService menuService;

    private UUID restaurantId;
    private UUID menuItemId;
    private Restaurant restaurant;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID();
        menuItemId = UUID.randomUUID();

        restaurant = new Restaurant("Test Restaurant", "Description", 4.5, 30, Collections.emptyList());
        ReflectionTestUtils.setField(restaurant, "id", restaurantId);

        menuItem = new MenuItem("Burger", "Beef burger", BigDecimal.valueOf(10.99), restaurant);
        ReflectionTestUtils.setField(menuItem, "id", menuItemId);
    }

    @Test
    void testGetMenuItems() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MenuItem> menuPage = new PageImpl<>(List.of(menuItem));

        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        when(menuItemRepository.findByRestaurant_Id(restaurantId, pageable)).thenReturn(menuPage);

        Page<MenuItem> result = menuService.getMenuItems(restaurantId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Burger", result.getContent().get(0).getName());

        verify(restaurantService, times(1)).getRestaurant(restaurantId);
        verify(menuItemRepository, times(1)).findByRestaurant_Id(restaurantId, pageable);
    }

    @Test
    void testGetMenuItem_Found() {
        when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));

        MenuItem result = menuService.getMenuItem(menuItemId);

        assertNotNull(result);
        assertEquals("Burger", result.getName());
    }

    @Test
    void testGetMenuItem_NotFound() {
        when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> menuService.getMenuItem(menuItemId));
    }

    @Test
    void testCreateMenuItem() {
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MenuItem result = menuService.createMenuItem(restaurantId, "Fries", "Crispy fries", BigDecimal.valueOf(4.99));

        assertEquals("Fries", result.getName());
        assertEquals(restaurant, result.getRestaurant());
        verify(menuItemRepository, times(1)).save(any(MenuItem.class));
    }

    @Test
    void testUpdateMenuItem() {
        when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MenuItem result = menuService.updateMenuItem(restaurantId, menuItemId, "Cheese Burger", "With cheese", BigDecimal.valueOf(12.99));

        assertEquals("Cheese Burger", result.getName());
        assertEquals(BigDecimal.valueOf(12.99), result.getPrice());
        verify(menuItemRepository, times(1)).save(menuItem);
    }

    @Test
    void testUpdateMenuItem_WrongRestaurant() {
        UUID otherRestaurantId = UUID.randomUUID();
        when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));

        assertThrows(ResponseStatusException.class, () -> 
            menuService.updateMenuItem(otherRestaurantId, menuItemId, "Cheese Burger", "With cheese", BigDecimal.valueOf(12.99))
        );
    }

    @Test
    void testDeleteMenuItem() {
        when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));

        menuService.deleteMenuItem(restaurantId, menuItemId);

        verify(menuItemRepository, times(1)).deleteById(menuItemId);
    }

    @Test
    void testDeleteMenuItem_WrongRestaurant() {
        UUID otherRestaurantId = UUID.randomUUID();
        when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));

        assertThrows(ResponseStatusException.class, () -> 
            menuService.deleteMenuItem(otherRestaurantId, menuItemId)
        );
    }
}

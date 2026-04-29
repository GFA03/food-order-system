package com.omnieats.restaurant_service.service;

import com.omnieats.restaurant_service.model.MenuItem;
import com.omnieats.restaurant_service.model.Restaurant;
import com.omnieats.restaurant_service.repository.MenuItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantService restaurantService;

    public MenuService(MenuItemRepository menuItemRepository, RestaurantService restaurantService) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantService = restaurantService;
    }

    public Page<MenuItem> getMenuItems(UUID restaurantId, Pageable pageable) {
        // Optional: verify restaurant exists
        restaurantService.getRestaurant(restaurantId);
        return menuItemRepository.findByRestaurantId(restaurantId, pageable);
    }

    public MenuItem getMenuItem(UUID id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu item not found"));
    }

    public MenuItem createMenuItem(UUID restaurantId, String name, String description, BigDecimal price) {
        Restaurant restaurant = restaurantService.getRestaurant(restaurantId);
        MenuItem item = new MenuItem(name, description, price, restaurant);
        return menuItemRepository.save(item);
    }

    public MenuItem updateMenuItem(UUID restaurantId, UUID id, String name, String description, BigDecimal price) {
        MenuItem item = getMenuItem(id);
        if (!item.getRestaurantId().equals(restaurantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu item does not belong to the given restaurant");
        }
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        return menuItemRepository.save(item);
    }

    public void deleteMenuItem(UUID restaurantId, UUID id) {
        MenuItem item = getMenuItem(id);
        if (!item.getRestaurantId().equals(restaurantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu item does not belong to the given restaurant");
        }
        menuItemRepository.deleteById(id);
    }
}

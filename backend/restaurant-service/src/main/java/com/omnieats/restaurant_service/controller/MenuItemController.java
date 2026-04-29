package com.omnieats.restaurant_service.controller;

import com.omnieats.restaurant_service.model.MenuItem;
import com.omnieats.restaurant_service.service.MenuService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/menu")
public class MenuItemController {

    private final MenuService menuService;

    public MenuItemController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ResponseEntity<Page<MenuItem>> getMenuItems(@PathVariable UUID restaurantId, Pageable pageable) {
        return ResponseEntity.ok(menuService.getMenuItems(restaurantId, pageable));
    }

    record CreateMenuItemRequest(String name, String description, BigDecimal price) {}

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<MenuItem> createMenuItem(@PathVariable UUID restaurantId, @RequestBody CreateMenuItemRequest request) {
        MenuItem item = menuService.createMenuItem(restaurantId, request.name(), request.description(), request.price());
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<MenuItem> updateMenuItem(@PathVariable UUID restaurantId, @PathVariable UUID id, @RequestBody CreateMenuItemRequest request) {
        MenuItem item = menuService.updateMenuItem(restaurantId, id, request.name(), request.description(), request.price());
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable UUID restaurantId, @PathVariable UUID id) {
        menuService.deleteMenuItem(restaurantId, id);
        return ResponseEntity.noContent().build();
    }
}

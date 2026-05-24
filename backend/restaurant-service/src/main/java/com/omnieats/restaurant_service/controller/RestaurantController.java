package com.omnieats.restaurant_service.controller;

import com.omnieats.restaurant_service.dto.RestaurantSummaryDto;
import com.omnieats.restaurant_service.model.Restaurant;
import com.omnieats.restaurant_service.service.RestaurantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public ResponseEntity<Page<Restaurant>> getRestaurants(
            @RequestParam(required = false) List<UUID> tags,
            Pageable pageable) {
        return ResponseEntity.ok(restaurantService.getRestaurants(tags, pageable));
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<RestaurantSummaryDto>> getTopRated() {
        return ResponseEntity.ok(restaurantService.getTopRated());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getRestaurant(@PathVariable UUID id) {
        return ResponseEntity.ok(restaurantService.getRestaurant(id));
    }

    record CreateRestaurantRequest(
            @NotBlank String name,
            @Size(max = 1000) String description,
            @DecimalMin("0.0") @DecimalMax("5.0") Double rating,
            @Positive Integer deliveryTime,
            List<UUID> cuisineTagIds) {}

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Restaurant> createRestaurant(@Valid @RequestBody CreateRestaurantRequest request) {
        Restaurant restaurant = restaurantService.createRestaurant(
                request.name(), request.description(), request.rating(), request.deliveryTime(), request.cuisineTagIds()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurant);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Restaurant> updateRestaurant(@PathVariable UUID id, @Valid @RequestBody CreateRestaurantRequest request) {
        Restaurant restaurant = restaurantService.updateRestaurant(
                id, request.name(), request.description(), request.rating(), request.deliveryTime(), request.cuisineTagIds()
        );
        return ResponseEntity.ok(restaurant);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable UUID id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }
}

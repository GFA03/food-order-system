package com.omnieats.restaurant_service.repository;

import com.omnieats.restaurant_service.model.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    Page<MenuItem> findByRestaurant_Id(UUID restaurantId, Pageable pageable);
}

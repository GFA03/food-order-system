package com.omnieats.restaurant_service.repository;

import com.omnieats.restaurant_service.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    @Query("SELECT DISTINCT r FROM Restaurant r JOIN r.cuisineTags t WHERE t.id IN :tagIds")
    Page<Restaurant> findByCuisineTagsIdIn(@Param("tagIds") List<UUID> tagIds, Pageable pageable);

}

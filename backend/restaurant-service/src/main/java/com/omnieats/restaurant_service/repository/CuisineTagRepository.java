package com.omnieats.restaurant_service.repository;

import com.omnieats.restaurant_service.model.CuisineTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CuisineTagRepository extends JpaRepository<CuisineTag, UUID> {
}

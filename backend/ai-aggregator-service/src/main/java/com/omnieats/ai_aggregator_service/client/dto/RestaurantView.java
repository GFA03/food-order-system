package com.omnieats.ai_aggregator_service.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Read model of a restaurant as exposed by {@code GET /api/restaurants}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RestaurantView(
        String id,
        String name,
        String description,
        Double rating,
        Integer deliveryTime,
        List<CuisineTagView> cuisineTags
) {
    public List<CuisineTagView> cuisineTagsOrEmpty() {
        return cuisineTags == null ? List.of() : cuisineTags;
    }
}

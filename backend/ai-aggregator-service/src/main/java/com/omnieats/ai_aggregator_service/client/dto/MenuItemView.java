package com.omnieats.ai_aggregator_service.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Read model of a menu item as exposed by {@code GET /api/restaurants/{id}/menu}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MenuItemView(
        String id,
        String name,
        String description,
        Double price,
        String restaurantId
) {
}

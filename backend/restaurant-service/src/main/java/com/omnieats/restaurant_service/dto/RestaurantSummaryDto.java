package com.omnieats.restaurant_service.dto;

import com.omnieats.restaurant_service.model.Restaurant;

import java.util.List;

/**
 * Cache-friendly summary of a restaurant returned by the "top rated" endpoint.
 *
 * <p>We cache DTOs rather than JPA entities so the Redis JSON round-trips cleanly
 * (entities have no id setter and lazy/proxy collections that don't deserialize).
 */
public record RestaurantSummaryDto(
        String id,
        String name,
        String description,
        Double rating,
        Integer deliveryTime,
        List<CuisineTagDto> cuisineTags
) {
    public static RestaurantSummaryDto from(Restaurant r) {
        return new RestaurantSummaryDto(
                r.getId() == null ? null : r.getId().toString(),
                r.getName(),
                r.getDescription(),
                r.getRating(),
                r.getDeliveryTime(),
                r.getCuisineTags().stream().map(CuisineTagDto::from).toList());
    }
}

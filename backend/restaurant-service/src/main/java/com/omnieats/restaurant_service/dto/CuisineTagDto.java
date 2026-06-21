package com.omnieats.restaurant_service.dto;

import com.omnieats.restaurant_service.model.CuisineTag;

/** Cache-friendly view of a cuisine tag (records serialize cleanly to/from Redis JSON). */
public record CuisineTagDto(String id, String name) {

    public static CuisineTagDto from(CuisineTag tag) {
        return new CuisineTagDto(
                tag.getId() == null ? null : tag.getId().toString(),
                tag.getName());
    }
}

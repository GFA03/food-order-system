package com.omnieats.ai_aggregator_service.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Minimal view of Spring Data's {@code Page<T>} JSON returned by restaurant-service.
 * Only {@code content} is needed here; all paging metadata is ignored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PageResponse<T>(List<T> content) {

    public List<T> contentOrEmpty() {
        return content == null ? List.of() : content;
    }
}

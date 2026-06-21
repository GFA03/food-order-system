package com.omnieats.ai_aggregator_service.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CuisineTagView(String id, String name) {
}

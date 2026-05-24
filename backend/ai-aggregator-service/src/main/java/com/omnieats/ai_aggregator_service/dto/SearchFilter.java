package com.omnieats.ai_aggregator_service.dto;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * Structured filter the LLM extracts from the user's free-text prompt.
 *
 * <p>This is the target type passed to Spring AI's {@code ChatClient...entity(SearchFilter.class)};
 * the field descriptions double as the JSON-schema hints sent to the model. All fields are
 * optional — the model omits what the prompt doesn't mention.
 */
@JsonClassDescription("Structured food-search filter extracted from a user's natural language request")
public record SearchFilter(
        @JsonProperty(required = false)
        @JsonPropertyDescription("Free-text keywords describing the desired food or restaurant, e.g. [\"ramen\", \"spicy\"]")
        List<String> keywords,

        @JsonProperty(required = false)
        @JsonPropertyDescription("Cuisine categories mentioned, e.g. [\"Italian\", \"Vegan\", \"Japanese\"]")
        List<String> cuisineTags,

        @JsonProperty(required = false)
        @JsonPropertyDescription("Dietary requirements, e.g. [\"vegan\", \"vegetarian\", \"gluten-free\"]")
        List<String> dietary,

        @JsonProperty(required = false)
        @JsonPropertyDescription("Maximum price per item the user is willing to pay, if stated")
        Double maxPrice,

        @JsonProperty(required = false)
        @JsonPropertyDescription("True if the user explicitly wants spicy food")
        Boolean spicy
) {
    /** Null-safe accessor used throughout the service. */
    public List<String> keywordsOrEmpty() {
        return keywords == null ? List.of() : keywords;
    }

    public List<String> cuisineTagsOrEmpty() {
        return cuisineTags == null ? List.of() : cuisineTags;
    }

    public List<String> dietaryOrEmpty() {
        return dietary == null ? List.of() : dietary;
    }
}

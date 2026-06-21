package com.omnieats.ai_aggregator_service.dto;

/**
 * A single suggestion returned to the frontend. Matches the {@code AiSuggestion}
 * TypeScript interface in {@code frontend/src/types/index.ts}.
 *
 * @param type        either {@code "restaurant"} or {@code "menuItem"}
 * @param id          id the frontend uses to deep-link (restaurant id in both cases)
 * @param name        display name
 * @param description short blurb
 * @param reason      why this was suggested for the user's prompt
 */
public record AiSuggestion(
        String type,
        String id,
        String name,
        String description,
        String reason
) {
    public static final String TYPE_RESTAURANT = "restaurant";
    public static final String TYPE_MENU_ITEM = "menuItem";
}

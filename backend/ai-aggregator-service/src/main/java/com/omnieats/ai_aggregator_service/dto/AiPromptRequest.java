package com.omnieats.ai_aggregator_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Incoming natural-language search request from the frontend.
 *
 * <p>Contract: {@code POST /api/ai/prompt  { "message": "..." }}.
 */
public record AiPromptRequest(
        @NotBlank(message = "message must not be blank")
        @Size(max = 500, message = "message must be at most 500 characters")
        String message
) {
}

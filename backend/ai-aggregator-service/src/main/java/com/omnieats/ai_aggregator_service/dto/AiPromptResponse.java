package com.omnieats.ai_aggregator_service.dto;

import java.util.List;

/**
 * Response body for {@code POST /api/ai/prompt}: {@code { "suggestions": [...] }}.
 */
public record AiPromptResponse(List<AiSuggestion> suggestions) {
}

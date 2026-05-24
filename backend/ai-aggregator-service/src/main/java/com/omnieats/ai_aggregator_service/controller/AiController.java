package com.omnieats.ai_aggregator_service.controller;

import com.omnieats.ai_aggregator_service.dto.AiPromptRequest;
import com.omnieats.ai_aggregator_service.dto.AiPromptResponse;
import com.omnieats.ai_aggregator_service.dto.AiSuggestion;
import com.omnieats.ai_aggregator_service.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * Natural-language food search. The gateway forwards the authenticated user via the
     * {@code X-User-Id} header; it is optional here so the endpoint stays testable.
     */
    @PostMapping("/prompt")
    public ResponseEntity<AiPromptResponse> prompt(
            @Valid @RequestBody AiPromptRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        List<AiSuggestion> suggestions = aiService.suggest(request.message(), userId);
        return ResponseEntity.ok(new AiPromptResponse(suggestions));
    }
}

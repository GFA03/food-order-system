package com.omnieats.ai_aggregator_service.service;

import com.omnieats.ai_aggregator_service.dto.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Wraps the local LLM (Ollama, via Spring AI). The extracted {@link SearchFilter} is the
 * expensive part of a request, so it is cached in Redis keyed by the normalised prompt —
 * a short-term cache of AI responses that makes repeat/similar searches near-instant.
 *
 * <p>Kept in its own bean so the {@code @Cacheable} proxy applies when {@code AiService}
 * calls it. On an LLM failure the exception propagates (and is not cached), letting the
 * circuit breaker in {@code AiService} fall back to keyword search.
 */
@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    private static final String SYSTEM_PROMPT = """
            You are a food-search assistant for a restaurant marketplace.
            Extract the user's intent into the structured filter.
            Only fill fields the user actually expressed; leave the rest empty.
            Normalise cuisine names to capitalised singular nouns (e.g. "Italian", "Vegan").
            Do not invent dishes or restaurants.""";

    private final ChatClient chatClient;

    public LlmService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Cacheable(value = "aiFilters", key = "#prompt == null ? '' : #prompt.toLowerCase().trim()")
    public SearchFilter extractFilter(String prompt) {
        log.debug("LLM extracting filter (cache miss) for prompt='{}'", prompt);
        SearchFilter filter = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(prompt)
                .call()
                .entity(SearchFilter.class);
        log.debug("LLM extracted filter: {}", filter);
        return filter;
    }
}

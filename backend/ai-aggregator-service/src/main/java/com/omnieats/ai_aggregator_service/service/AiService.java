package com.omnieats.ai_aggregator_service.service;

import com.omnieats.ai_aggregator_service.client.RestaurantClient;
import com.omnieats.ai_aggregator_service.client.dto.CuisineTagView;
import com.omnieats.ai_aggregator_service.client.dto.MenuItemView;
import com.omnieats.ai_aggregator_service.client.dto.RestaurantView;
import com.omnieats.ai_aggregator_service.dto.AiSuggestion;
import com.omnieats.ai_aggregator_service.dto.SearchFilter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Turns a natural-language food request into ranked suggestions.
 *
 * <p>The primary path asks a local LLM (Ollama, via Spring AI) to extract a structured
 * {@link SearchFilter}, then aggregates live data from restaurant-service and ranks it
 * (Aggregator pattern). If the LLM is slow or unavailable the {@code ollama} circuit
 * breaker trips and {@link #keywordFallback} provides a plain keyword search instead, so
 * the endpoint stays useful with no model running.
 */
@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private static final int MAX_SUGGESTIONS = 6;

    private final LlmService llmService;
    private final RestaurantClient restaurantClient;

    public AiService(LlmService llmService, RestaurantClient restaurantClient) {
        this.llmService = llmService;
        this.restaurantClient = restaurantClient;
    }

    /**
     * Primary entry point. The circuit breaker wraps the LLM call; any failure (model down,
     * timeout, open circuit) routes to {@link #keywordFallback}. The filter extraction itself
     * is Redis-cached per prompt (see {@link LlmService}).
     */
    @CircuitBreaker(name = "ollama", fallbackMethod = "keywordFallback")
    public List<AiSuggestion> suggest(String prompt, String userId) {
        log.debug("AI suggest for user={} prompt='{}'", userId, prompt);

        SearchFilter filter = llmService.extractFilter(prompt);

        Set<String> terms = termsFrom(filter);
        if (terms.isEmpty()) {
            // Model returned nothing usable — fall back to the raw prompt words.
            terms = tokenize(prompt);
        }
        boolean spicy = filter != null && Boolean.TRUE.equals(filter.spicy());
        Double maxPrice = filter == null ? null : filter.maxPrice();

        return rank(terms, spicy, maxPrice);
    }

    /**
     * Resilience4j fallback: pure keyword search over the catalogue, no LLM involved.
     * The trailing {@link Throwable} is required by resilience4j to bind the fallback.
     */
    @SuppressWarnings("unused")
    public List<AiSuggestion> keywordFallback(String prompt, String userId, Throwable t) {
        log.warn("LLM unavailable ({}); falling back to keyword search for prompt='{}'",
                t.getClass().getSimpleName(), prompt);
        return rank(tokenize(prompt), false, null);
    }

    // ── ranking ────────────────────────────────────────────────────────────────

    private List<AiSuggestion> rank(Set<String> terms, boolean spicy, Double maxPrice) {
        List<RestaurantView> restaurants = restaurantClient.fetchRestaurants();
        List<MenuItemView> menuItems = restaurantClient.fetchAllMenuItems(restaurants);

        List<Scored> scored = new ArrayList<>();

        for (RestaurantView r : restaurants) {
            List<String> tagNames = r.cuisineTagsOrEmpty().stream().map(CuisineTagView::name).toList();
            String haystack = text(r.name(), r.description(), String.join(" ", tagNames));
            List<String> matched = matchedTerms(haystack, terms);
            if (spicy && containsWord(haystack, "spicy")) {
                matched.add("spicy");
            }
            if (matched.isEmpty()) {
                continue;
            }
            double score = matched.size() + ratingBoost(r.rating());
            scored.add(new Scored(score, new AiSuggestion(
                    AiSuggestion.TYPE_RESTAURANT,
                    r.id(),
                    r.name(),
                    r.description(),
                    restaurantReason(matched, r.rating()))));
        }

        for (MenuItemView m : menuItems) {
            String haystack = text(m.name(), m.description(), "");
            List<String> matched = matchedTerms(haystack, terms);
            if (spicy && containsWord(haystack, "spicy")) {
                matched.add("spicy");
            }
            if (matched.isEmpty()) {
                continue;
            }
            if (maxPrice != null && m.price() != null && m.price() > maxPrice) {
                continue; // over budget
            }
            double score = matched.size() + (maxPrice != null && m.price() != null ? 0.5 : 0);
            scored.add(new Scored(score, new AiSuggestion(
                    AiSuggestion.TYPE_MENU_ITEM,
                    m.restaurantId(), // frontend deep-links menu items to their restaurant
                    m.name(),
                    m.description(),
                    menuItemReason(matched, m.price()))));
        }

        return scored.stream()
                .sorted(Comparator.comparingDouble(Scored::score).reversed())
                .limit(MAX_SUGGESTIONS)
                .map(Scored::suggestion)
                .toList();
    }

    private static double ratingBoost(Double rating) {
        return rating != null && rating >= 4.0 ? 1.0 : 0.0;
    }

    private static String restaurantReason(List<String> matched, Double rating) {
        StringBuilder sb = new StringBuilder("Matches your request for ").append(String.join(", ", matched));
        if (rating != null && rating >= 4.0) {
            sb.append(String.format(" — highly rated (%.1f★)", rating));
        }
        return sb.toString();
    }

    private static String menuItemReason(List<String> matched, Double price) {
        StringBuilder sb = new StringBuilder("Dish matching ").append(String.join(", ", matched));
        if (price != null) {
            sb.append(String.format(" at $%.2f", price));
        }
        return sb.toString();
    }

    // ── term extraction & matching ───────────────────────────────────────────────

    private static Set<String> termsFrom(SearchFilter filter) {
        if (filter == null) {
            return Set.of();
        }
        Set<String> terms = new LinkedHashSet<>();
        terms.addAll(filter.keywordsOrEmpty());
        terms.addAll(filter.cuisineTagsOrEmpty());
        terms.addAll(filter.dietaryOrEmpty());
        return normalise(terms);
    }

    private static Set<String> tokenize(String prompt) {
        if (prompt == null) {
            return Set.of();
        }
        // Split on non-letters; drop short stop-word-ish tokens.
        return normalise(Arrays.stream(prompt.toLowerCase().split("[^a-z]+"))
                .filter(w -> w.length() > 2)
                .filter(w -> !STOP_WORDS.contains(w))
                .toList());
    }

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "want", "with", "something", "some", "food", "give", "find",
            "looking", "would", "like", "near", "show", "any", "that", "this", "have");

    private static Set<String> normalise(Iterable<String> raw) {
        Set<String> out = new LinkedHashSet<>();
        for (String s : raw) {
            if (s != null && !s.isBlank()) {
                out.add(s.trim().toLowerCase());
            }
        }
        return out;
    }

    private static List<String> matchedTerms(String haystack, Set<String> terms) {
        String lower = haystack.toLowerCase();
        List<String> matched = new ArrayList<>();
        for (String term : terms) {
            if (lower.contains(term)) {
                matched.add(term);
            }
        }
        return matched;
    }

    private static boolean containsWord(String haystack, String word) {
        return haystack.toLowerCase().contains(word);
    }

    private static String text(String a, String b, String c) {
        return (nullToEmpty(a) + " " + nullToEmpty(b) + " " + nullToEmpty(c)).trim();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /** Score wrapper for ranking. */
    private record Scored(double score, AiSuggestion suggestion) {
    }
}

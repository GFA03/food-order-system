package com.omnieats.ai_aggregator_service.client;

import com.omnieats.ai_aggregator_service.client.dto.MenuItemView;
import com.omnieats.ai_aggregator_service.client.dto.PageResponse;
import com.omnieats.ai_aggregator_service.client.dto.RestaurantView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

/**
 * Thin read-only client over restaurant-service. The base URL is profile-driven
 * (see {@code restaurant-service.base-url}), so this doubles as our service-discovery
 * mechanism without a registry.
 *
 * <p>Failures are logged and degrade to empty results rather than propagating, so the
 * AI endpoint always returns a well-formed (possibly empty) suggestion list.
 */
@Component
public class RestaurantClient {

    private static final Logger log = LoggerFactory.getLogger(RestaurantClient.class);

    /** Pull a generous page so the aggregator sees the full (demo-sized) catalogue. */
    private static final int PAGE_SIZE = 100;

    private final RestClient restClient;

    public RestaurantClient(RestClient restaurantRestClient) {
        this.restClient = restaurantRestClient;
    }

    /** All restaurants (first page, up to {@link #PAGE_SIZE}). */
    public List<RestaurantView> fetchRestaurants() {
        try {
            PageResponse<RestaurantView> page = restClient.get()
                    .uri(uri -> uri.path("/api/restaurants").queryParam("size", PAGE_SIZE).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            return page == null ? List.of() : page.contentOrEmpty();
        } catch (RestClientException e) {
            log.warn("Failed to fetch restaurants from restaurant-service: {}", e.getMessage());
            return List.of();
        }
    }

    /** Menu items for a single restaurant. */
    public List<MenuItemView> fetchMenuItems(String restaurantId) {
        try {
            PageResponse<MenuItemView> page = restClient.get()
                    .uri(uri -> uri.path("/api/restaurants/{id}/menu")
                            .queryParam("size", PAGE_SIZE)
                            .build(restaurantId))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            return page == null ? List.of() : page.contentOrEmpty();
        } catch (RestClientException e) {
            log.warn("Failed to fetch menu for restaurant {}: {}", restaurantId, e.getMessage());
            return List.of();
        }
    }

    /** Every menu item across all restaurants — the combined pool for the aggregator. */
    public List<MenuItemView> fetchAllMenuItems(List<RestaurantView> restaurants) {
        List<MenuItemView> all = new ArrayList<>();
        for (RestaurantView r : restaurants) {
            all.addAll(fetchMenuItems(r.id()));
        }
        return all;
    }
}

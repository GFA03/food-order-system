package com.omnieats.ai_aggregator_service.service;

import com.omnieats.ai_aggregator_service.client.RestaurantClient;
import com.omnieats.ai_aggregator_service.client.dto.CuisineTagView;
import com.omnieats.ai_aggregator_service.client.dto.MenuItemView;
import com.omnieats.ai_aggregator_service.client.dto.RestaurantView;
import com.omnieats.ai_aggregator_service.dto.AiSuggestion;
import com.omnieats.ai_aggregator_service.dto.SearchFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiServiceTest {

    private ChatClient chatClient;
    private RestaurantClient restaurantClient;
    private AiService aiService;

    private final RestaurantView greenGarden = new RestaurantView(
            "r2", "Green Garden", "Fresh vegan dishes", 4.6, 20,
            List.of(new CuisineTagView("t1", "Vegan")));
    private final RestaurantView bellaItalia = new RestaurantView(
            "r1", "Bella Italia", "Authentic Italian cuisine", 4.4, 30,
            List.of(new CuisineTagView("t2", "Italian")));

    private final MenuItemView veganBurger = new MenuItemView(
            "m1", "Vegan Burger", "Plant-based patty", 12.0, "r2");
    private final MenuItemView pizza = new MenuItemView(
            "m2", "Margherita Pizza", "Tomato and basil", 10.0, "r1");

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        restaurantClient = mock(RestaurantClient.class);
        aiService = new AiService(chatClient, restaurantClient);

        when(restaurantClient.fetchRestaurants()).thenReturn(List.of(greenGarden, bellaItalia));
        when(restaurantClient.fetchAllMenuItems(any())).thenReturn(List.of(veganBurger, pizza));
    }

    @Test
    void suggest_usesLlmFilter_andRanksMatchingCatalogue() {
        SearchFilter filter = new SearchFilter(
                List.of("vegan"), List.of("Vegan"), List.of("vegan"), null, false);
        when(chatClient.prompt().system(anyString()).user(anyString()).call().entity(SearchFilter.class))
                .thenReturn(filter);

        List<AiSuggestion> result = aiService.suggest("I want something vegan", "user-1");

        assertThat(result).isNotEmpty();
        // The vegan restaurant is suggested...
        assertThat(result)
                .anySatisfy(s -> {
                    assertThat(s.type()).isEqualTo(AiSuggestion.TYPE_RESTAURANT);
                    assertThat(s.name()).isEqualTo("Green Garden");
                    assertThat(s.id()).isEqualTo("r2");
                });
        // ...and the vegan dish, deep-linked to its restaurant id.
        assertThat(result)
                .anySatisfy(s -> {
                    assertThat(s.type()).isEqualTo(AiSuggestion.TYPE_MENU_ITEM);
                    assertThat(s.name()).isEqualTo("Vegan Burger");
                    assertThat(s.id()).isEqualTo("r2");
                });
        // The Italian-only restaurant must not match a vegan query.
        assertThat(result).noneSatisfy(s -> assertThat(s.name()).isEqualTo("Bella Italia"));
    }

    @Test
    void keywordFallback_searchesCatalogueWithoutLlm() {
        List<AiSuggestion> result =
                aiService.keywordFallback("looking for something vegan", "user-1",
                        new RuntimeException("ollama down"));

        assertThat(result).isNotEmpty();
        assertThat(result).anySatisfy(s -> assertThat(s.name()).isEqualTo("Green Garden"));
        // ChatClient is never consulted on the fallback path.
        org.mockito.Mockito.verifyNoInteractions(chatClient);
    }

    @Test
    void suggest_respectsMaxPrice_filteringOutExpensiveDishes() {
        SearchFilter filter = new SearchFilter(
                List.of("burger", "pizza"), null, null, 11.0, false);
        when(chatClient.prompt().system(anyString()).user(anyString()).call().entity(SearchFilter.class))
                .thenReturn(filter);

        List<AiSuggestion> result = aiService.suggest("cheap burger or pizza under 11", "user-1");

        // Pizza ($10) stays; Vegan Burger ($12) is over budget and dropped as a menu item.
        assertThat(result)
                .filteredOn(s -> s.type().equals(AiSuggestion.TYPE_MENU_ITEM))
                .noneSatisfy(s -> assertThat(s.name()).isEqualTo("Vegan Burger"));
    }
}

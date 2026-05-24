package com.omnieats.ai_aggregator_service.controller;

import com.omnieats.ai_aggregator_service.dto.AiSuggestion;
import com.omnieats.ai_aggregator_service.service.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Web-layer test; security filters disabled so we exercise the controller contract directly. */
@WebMvcTest(AiController.class)
@AutoConfigureMockMvc(addFilters = false)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiService aiService;

    @Test
    void prompt_returnsSuggestionsInExpectedShape() throws Exception {
        when(aiService.suggest(eq("vegan and spicy"), any()))
                .thenReturn(List.of(new AiSuggestion(
                        "restaurant", "r2", "Green Garden", "Fresh vegan dishes", "Fully vegan menu")));

        mockMvc.perform(post("/api/ai/prompt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "user-1")
                        .content("{\"message\":\"vegan and spicy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions").isArray())
                .andExpect(jsonPath("$.suggestions[0].type").value("restaurant"))
                .andExpect(jsonPath("$.suggestions[0].id").value("r2"))
                .andExpect(jsonPath("$.suggestions[0].name").value("Green Garden"))
                .andExpect(jsonPath("$.suggestions[0].reason").value("Fully vegan menu"));
    }

    @Test
    void prompt_rejectsBlankMessageWith400() throws Exception {
        mockMvc.perform(post("/api/ai/prompt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }
}

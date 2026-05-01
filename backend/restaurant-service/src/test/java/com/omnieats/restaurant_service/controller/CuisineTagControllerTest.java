package com.omnieats.restaurant_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnieats.restaurant_service.model.CuisineTag;
import com.omnieats.restaurant_service.service.CuisineTagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CuisineTagController.class)
@AutoConfigureMockMvc(addFilters = false)
class CuisineTagControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private CuisineTagService cuisineTagService;

    @Test
    void getAllTags_ReturnsTagList() throws Exception {
        CuisineTag tag = new CuisineTag("Italian");
        ReflectionTestUtils.setField(tag, "id", UUID.randomUUID());
        when(cuisineTagService.getAllTags()).thenReturn(List.of(tag));

        mockMvc.perform(get("/api/restaurants/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Italian"));
    }

    @Test
    void getAllTags_EmptyList_Returns200() throws Exception {
        when(cuisineTagService.getAllTags()).thenReturn(List.of());

        mockMvc.perform(get("/api/restaurants/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createTag_ValidRequest_Returns201() throws Exception {
        CuisineTag tag = new CuisineTag("Vegan");
        ReflectionTestUtils.setField(tag, "id", UUID.randomUUID());
        when(cuisineTagService.createTag("Vegan")).thenReturn(tag);

        mockMvc.perform(post("/api/restaurants/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Vegan\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Vegan"));
    }

    @Test
    void deleteTag_ExistingId_Returns204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(cuisineTagService).deleteTag(id);

        mockMvc.perform(delete("/api/restaurants/tags/{id}", id))
                .andExpect(status().isNoContent());

        verify(cuisineTagService).deleteTag(id);
    }
}

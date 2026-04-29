package com.omnieats.restaurant_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnieats.restaurant_service.model.MenuItem;
import com.omnieats.restaurant_service.model.Restaurant;
import com.omnieats.restaurant_service.service.MenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuItemController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MenuItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuService menuService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID restaurantId;
    private UUID menuItemId;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID();
        menuItemId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant("Test", "Desc", 4.0, 30, Collections.emptyList());
        ReflectionTestUtils.setField(restaurant, "id", restaurantId);

        menuItem = new MenuItem("Pizza", "Cheese Pizza", BigDecimal.valueOf(15.99), restaurant);
        ReflectionTestUtils.setField(menuItem, "id", menuItemId);
    }

    @Test
    void testGetMenuItems() throws Exception {
        Page<MenuItem> menuPage = new PageImpl<>(List.of(menuItem));
        when(menuService.getMenuItems(eq(restaurantId), any())).thenReturn(menuPage);

        mockMvc.perform(get("/api/restaurants/{restaurantId}/menu", restaurantId)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Pizza"));
    }

    @Test
    void testCreateMenuItem() throws Exception {
        when(menuService.createMenuItem(eq(restaurantId), eq("Pizza"), eq("Cheese Pizza"), eq(BigDecimal.valueOf(15.99))))
                .thenReturn(menuItem);

        String requestJson = "{\"name\":\"Pizza\",\"description\":\"Cheese Pizza\",\"price\":15.99}";

        mockMvc.perform(post("/api/restaurants/{restaurantId}/menu", restaurantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Pizza"));
    }

    @Test
    void testUpdateMenuItem() throws Exception {
        when(menuService.updateMenuItem(eq(restaurantId), eq(menuItemId), eq("Pizza Updated"), eq("Cheese Pizza Updated"), eq(BigDecimal.valueOf(17.99))))
                .thenReturn(new MenuItem("Pizza Updated", "Cheese Pizza Updated", BigDecimal.valueOf(17.99), null));

        String requestJson = "{\"name\":\"Pizza Updated\",\"description\":\"Cheese Pizza Updated\",\"price\":17.99}";

        mockMvc.perform(put("/api/restaurants/{restaurantId}/menu/{id}", restaurantId, menuItemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pizza Updated"));
    }

    @Test
    void testDeleteMenuItem() throws Exception {
        mockMvc.perform(delete("/api/restaurants/{restaurantId}/menu/{id}", restaurantId, menuItemId))
                .andExpect(status().isNoContent());

        Mockito.verify(menuService, Mockito.times(1)).deleteMenuItem(restaurantId, menuItemId);
    }
}

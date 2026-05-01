package com.omnieats.restaurant_service.controller;

import com.omnieats.restaurant_service.model.CuisineTag;
import com.omnieats.restaurant_service.model.Restaurant;
import com.omnieats.restaurant_service.repository.CuisineTagRepository;
import com.omnieats.restaurant_service.repository.MenuItemRepository;
import com.omnieats.restaurant_service.repository.RestaurantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RestaurantControllerIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private RestaurantRepository restaurantRepository;
    @Autowired private CuisineTagRepository cuisineTagRepository;
    @Autowired private MenuItemRepository menuItemRepository;

    private Restaurant testRestaurant;
    private CuisineTag testTag;

    @BeforeEach
    void setUp() {
        testTag = cuisineTagRepository.save(new CuisineTag("IntegrationTestTag_" + UUID.randomUUID()));
        testRestaurant = restaurantRepository.save(
                new Restaurant("Integration Test Restaurant", "Test Description", 4.5, 30, List.of(testTag)));
    }

    @AfterEach
    void tearDown() {
        menuItemRepository.findByRestaurant_Id(testRestaurant.getId(),
                org.springframework.data.domain.PageRequest.of(0, 100))
                .forEach(menuItemRepository::delete);
        restaurantRepository.delete(testRestaurant);
        cuisineTagRepository.delete(testTag);
    }

    @Test
    void getRestaurants_ReturnsPagedList() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/restaurants", Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("content"));
    }

    @Test
    void getRestaurant_ById_ReturnsRestaurant() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/restaurants/" + testRestaurant.getId(), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Integration Test Restaurant", response.getBody().get("name"));
    }

    @Test
    void getRestaurant_NotFound_Returns404() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/restaurants/" + UUID.randomUUID(), Map.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getRestaurants_FilterByTag_ReturnsMatchingRestaurants() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/restaurants?tags=" + testTag.getId(), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<?> content = (List<?>) response.getBody().get("content");
        assertTrue(content.size() >= 1);
    }

    @Test
    void createRestaurant_AsAdmin_Returns201() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", UUID.randomUUID().toString());
        headers.set("X-User-Roles", "ADMIN");

        Map<String, Object> body = Map.of(
                "name", "Admin Created Restaurant",
                "description", "Created by admin test",
                "rating", 4.0,
                "deliveryTime", 25,
                "cuisineTagIds", List.of()
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/restaurants", request, Map.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Admin Created Restaurant", response.getBody().get("name"));

        // cleanup
        String id = (String) response.getBody().get("id");
        if (id != null) restaurantRepository.deleteById(UUID.fromString(id));
    }

    @Test
    void createRestaurant_WithoutAuth_Returns403() {
        Map<String, Object> body = Map.of(
                "name", "Unauthorized Restaurant",
                "description", "Should be rejected",
                "rating", 4.0,
                "deliveryTime", 25,
                "cuisineTagIds", List.of()
        );

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/restaurants", body, Map.class);

        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    void deleteRestaurant_AsAdmin_Returns204() {
        Restaurant toDelete = restaurantRepository.save(
                new Restaurant("To Be Deleted", "Temp", 3.0, 10, List.of()));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", UUID.randomUUID().toString());
        headers.set("X-User-Roles", "ADMIN");

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/restaurants/" + toDelete.getId(),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(restaurantRepository.existsById(toDelete.getId()));
    }
}

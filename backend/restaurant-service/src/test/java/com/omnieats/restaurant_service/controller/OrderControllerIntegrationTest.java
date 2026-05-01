package com.omnieats.restaurant_service.controller;

import com.omnieats.restaurant_service.model.MenuItem;
import com.omnieats.restaurant_service.model.Order;
import com.omnieats.restaurant_service.model.OrderStatus;
import com.omnieats.restaurant_service.model.Restaurant;
import com.omnieats.restaurant_service.repository.MenuItemRepository;
import com.omnieats.restaurant_service.repository.OrderRepository;
import com.omnieats.restaurant_service.repository.RestaurantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private OrderRepository orderRepository;
    @Autowired private RestaurantRepository restaurantRepository;
    @Autowired private MenuItemRepository menuItemRepository;

    private UUID userId;
    private Restaurant restaurant;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        restaurant = restaurantRepository.save(
                new Restaurant("Order Integration Restaurant", "Desc", 4.0, 20, List.of()));
        menuItem = menuItemRepository.save(
                new MenuItem("Test Dish", "Desc", new BigDecimal("9.99"), restaurant));
    }

    @AfterEach
    void tearDown() {
        orderRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 100))
                .forEach(orderRepository::delete);
        menuItemRepository.delete(menuItem);
        restaurantRepository.delete(restaurant);
    }

    @Test
    void createOrder_WithAuth_Returns201() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", userId.toString());
        headers.set("X-User-Roles", "USER");

        Map<String, Object> body = Map.of(
                "restaurantId", restaurant.getId().toString(),
                "items", List.of(Map.of(
                        "menuItemId", menuItem.getId().toString(),
                        "quantity", 2))
        );

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/orders",
                new HttpEntity<>(body, headers), Map.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("PENDING", response.getBody().get("status"));
        assertNotNull(response.getBody().get("id"));
    }

    @Test
    void createOrder_WithoutAuth_Returns4xx() {
        Map<String, Object> body = Map.of(
                "restaurantId", restaurant.getId().toString(),
                "items", List.of(Map.of(
                        "menuItemId", menuItem.getId().toString(),
                        "quantity", 1))
        );

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/orders", body, Map.class);

        // Spring Security returns 403 for anonymous users hitting authenticated() rule
        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    void getOrders_WithAuth_ReturnsList() {
        orderRepository.save(new Order(userId, restaurant.getId(), restaurant.getName(), new BigDecimal("9.99")));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        headers.set("X-User-Roles", "USER");

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/orders", HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<?> content = (List<?>) response.getBody().get("content");
        assertEquals(1, content.size());
    }

    @Test
    void getOrder_ById_WithAuth_ReturnsOrder() {
        Order order = orderRepository.save(
                new Order(userId, restaurant.getId(), restaurant.getName(), new BigDecimal("9.99")));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        headers.set("X-User-Roles", "USER");

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/orders/" + order.getId(), HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(order.getId().toString(), response.getBody().get("id"));
    }

    @Test
    void getOrder_WithWrongUser_Returns404() {
        Order order = orderRepository.save(
                new Order(userId, restaurant.getId(), restaurant.getName(), new BigDecimal("9.99")));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", UUID.randomUUID().toString()); // different user
        headers.set("X-User-Roles", "USER");

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/orders/" + order.getId(), HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateOrderStatus_AsAdmin_ReturnsConfirmed() {
        Order order = orderRepository.save(
                new Order(userId, restaurant.getId(), restaurant.getName(), new BigDecimal("9.99")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", UUID.randomUUID().toString());
        headers.set("X-User-Roles", "ADMIN");

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/orders/admin/" + order.getId() + "/status",
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("status", "CONFIRMED"), headers),
                Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("CONFIRMED", response.getBody().get("status"));
    }

    @Test
    void getAllOrders_AsAdmin_ReturnsAll() {
        orderRepository.save(new Order(userId, restaurant.getId(), restaurant.getName(), new BigDecimal("5.00")));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", UUID.randomUUID().toString());
        headers.set("X-User-Roles", "ADMIN");

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/orders/admin", HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().containsKey("content"));
    }
}

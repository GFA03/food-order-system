package com.omnieats.restaurant_service.repository;

import com.omnieats.restaurant_service.model.Order;
import com.omnieats.restaurant_service.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired private OrderRepository orderRepository;

    private UUID userId;
    private UUID restaurantId;
    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();
        order1 = orderRepository.save(new Order(userId, restaurantId, "Burger Joint", new BigDecimal("15.99")));
        order2 = orderRepository.save(new Order(userId, restaurantId, "Burger Joint", new BigDecimal("8.50")));
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_ReturnsUserOrders() {
        Page<Order> result = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_OtherUser_ReturnsEmpty() {
        Page<Order> result = orderRepository.findByUserIdOrderByCreatedAtDesc(UUID.randomUUID(), PageRequest.of(0, 10));

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void findByIdAndUserId_Found() {
        Optional<Order> result = orderRepository.findByIdAndUserId(order1.getId(), userId);

        assertTrue(result.isPresent());
        assertEquals(order1.getId(), result.get().getId());
    }

    @Test
    void findByIdAndUserId_WrongUser_ReturnsEmpty() {
        Optional<Order> result = orderRepository.findByIdAndUserId(order1.getId(), UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdAndUserId_WrongId_ReturnsEmpty() {
        Optional<Order> result = orderRepository.findByIdAndUserId(UUID.randomUUID(), userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByOrderByCreatedAtDesc_ReturnsAllOrders() {
        Page<Order> result = orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void save_DefaultStatus_IsPending() {
        assertEquals(OrderStatus.PENDING, order1.getStatus());
    }

    @Test
    void save_UpdateStatus_Persists() {
        order1.setStatus(OrderStatus.CONFIRMED);
        Order updated = orderRepository.save(order1);

        assertEquals(OrderStatus.CONFIRMED, updated.getStatus());
    }
}

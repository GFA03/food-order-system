package com.omnieats.restaurant_service.service;

import com.omnieats.restaurant_service.model.MenuItem;
import com.omnieats.restaurant_service.model.Order;
import com.omnieats.restaurant_service.model.OrderStatus;
import com.omnieats.restaurant_service.model.Restaurant;
import com.omnieats.restaurant_service.repository.MenuItemRepository;
import com.omnieats.restaurant_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private MenuItemRepository menuItemRepository;
    @Mock private RestaurantService restaurantService;
    @InjectMocks private OrderService orderService;

    private UUID userId;
    private UUID restaurantId;
    private UUID menuItemId;
    private Restaurant restaurant;
    private MenuItem menuItem;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();
        menuItemId = UUID.randomUUID();

        restaurant = new Restaurant("Burger Joint", "Burgers", 4.2, 20, Collections.emptyList());
        ReflectionTestUtils.setField(restaurant, "id", restaurantId);

        menuItem = new MenuItem("Cheeseburger", "Beef patty", new BigDecimal("8.99"), restaurant);
        ReflectionTestUtils.setField(menuItem, "id", menuItemId);

        savedOrder = new Order(userId, restaurantId, "Burger Joint", BigDecimal.ZERO);
        ReflectionTestUtils.setField(savedOrder, "id", UUID.randomUUID());
    }

    @Test
    void createOrder_ValidRequest_ReturnsOrder() {
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));

        Order result = orderService.createOrder(userId, restaurantId,
                List.of(new OrderService.OrderItemRequest(menuItemId, 2)));

        assertNotNull(result);
        verify(orderRepository, times(2)).save(any(Order.class));
    }

    @Test
    void createOrder_EmptyItems_Throws400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(userId, restaurantId, Collections.emptyList()));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void createOrder_NullItems_Throws400() {
        assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(userId, restaurantId, null));
    }

    @Test
    void createOrder_ZeroQuantity_Throws400() {
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(userId, restaurantId,
                        List.of(new OrderService.OrderItemRequest(menuItemId, 0))));
    }

    @Test
    void createOrder_MenuItemNotFound_Throws404() {
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(userId, restaurantId,
                        List.of(new OrderService.OrderItemRequest(menuItemId, 1))));
    }

    @Test
    void createOrder_ItemBelongsToDifferentRestaurant_Throws400() {
        UUID otherRestaurantId = UUID.randomUUID();
        Restaurant otherRestaurant = new Restaurant("Other", "Other", 4.0, 15, Collections.emptyList());
        ReflectionTestUtils.setField(otherRestaurant, "id", otherRestaurantId);
        MenuItem otherItem = new MenuItem("Salad", "Salad", new BigDecimal("5.00"), otherRestaurant);
        ReflectionTestUtils.setField(otherItem, "id", menuItemId);

        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(otherItem));

        assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(userId, restaurantId,
                        List.of(new OrderService.OrderItemRequest(menuItemId, 1))));
    }

    @Test
    void getOrders_ReturnsPagedResults() {
        Page<Order> page = new PageImpl<>(List.of(savedOrder));
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class))).thenReturn(page);

        Page<Order> result = orderService.getOrders(userId, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getOrder_Found_ReturnsOrder() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(savedOrder));

        Order result = orderService.getOrder(userId, orderId);

        assertNotNull(result);
    }

    @Test
    void getOrder_NotFound_Throws404() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> orderService.getOrder(userId, orderId));
    }

    @Test
    void updateOrderStatus_Success_ChangesStatus() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Order result = orderService.updateOrderStatus(orderId, OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
    }

    @Test
    void updateOrderStatus_NotFound_Throws404() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> orderService.updateOrderStatus(orderId, OrderStatus.CONFIRMED));
    }

    @Test
    void getAllOrders_ReturnsPaged() {
        Page<Order> page = new PageImpl<>(List.of(savedOrder));
        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(page);

        Page<Order> result = orderService.getAllOrders(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }
}

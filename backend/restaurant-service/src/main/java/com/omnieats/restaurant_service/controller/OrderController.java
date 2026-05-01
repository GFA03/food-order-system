package com.omnieats.restaurant_service.controller;

import com.omnieats.restaurant_service.model.Order;
import com.omnieats.restaurant_service.model.OrderStatus;
import com.omnieats.restaurant_service.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    record OrderItemRequest(String menuItemId, int quantity) {}

    record CreateOrderRequest(String restaurantId, List<OrderItemRequest> items) {}

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @AuthenticationPrincipal String userId,
            @RequestBody CreateOrderRequest request) {

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        List<OrderService.OrderItemRequest> serviceItems = request.items().stream()
                .map(i -> new OrderService.OrderItemRequest(UUID.fromString(i.menuItemId()), i.quantity()))
                .toList();

        Order order = orderService.createOrder(
                UUID.fromString(userId),
                UUID.fromString(request.restaurantId()),
                serviceItems
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    public ResponseEntity<Page<Order>> getOrders(
            @AuthenticationPrincipal String userId,
            Pageable pageable) {

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(orderService.getOrders(UUID.fromString(userId), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID id) {

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(orderService.getOrder(UUID.fromString(userId), id));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Order>> getAllOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    record UpdateOrderStatusRequest(OrderStatus status) {}

    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable UUID id,
            @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.status()));
    }
}

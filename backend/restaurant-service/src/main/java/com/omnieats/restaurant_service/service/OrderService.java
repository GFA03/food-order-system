package com.omnieats.restaurant_service.service;

import com.omnieats.restaurant_service.model.MenuItem;
import com.omnieats.restaurant_service.model.Order;
import com.omnieats.restaurant_service.model.OrderItem;
import com.omnieats.restaurant_service.model.OrderStatus;
import com.omnieats.restaurant_service.model.Restaurant;
import com.omnieats.restaurant_service.repository.MenuItemRepository;
import com.omnieats.restaurant_service.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    public record OrderItemRequest(UUID menuItemId, int quantity) {}

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantService restaurantService;

    public OrderService(OrderRepository orderRepository,
                        MenuItemRepository menuItemRepository,
                        RestaurantService restaurantService) {
        this.orderRepository = orderRepository;
        this.menuItemRepository = menuItemRepository;
        this.restaurantService = restaurantService;
    }

    @Transactional
    public Order createOrder(UUID userId, UUID restaurantId, List<OrderItemRequest> itemRequests) {
        if (itemRequests == null || itemRequests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must contain at least one item");
        }

        Restaurant restaurant = restaurantService.getRestaurant(restaurantId);

        Order order = new Order(userId, restaurantId, restaurant.getName(), BigDecimal.ZERO);
        order = orderRepository.save(order); // save first to get the ID for FK references

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest req : itemRequests) {
            if (req.quantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item quantity must be positive");
            }

            MenuItem menuItem = menuItemRepository.findById(req.menuItemId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Menu item not found: " + req.menuItemId()));

            // Validate the menu item belongs to this restaurant
            if (!menuItem.getRestaurantId().equals(restaurantId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Menu item " + req.menuItemId() + " does not belong to restaurant " + restaurantId);
            }

            OrderItem item = new OrderItem(
                    menuItem.getName(),
                    menuItem.getId(),
                    menuItem.getPrice(),
                    req.quantity(),
                    order
            );
            order.addItem(item);

            total = total.add(menuItem.getPrice().multiply(BigDecimal.valueOf(req.quantity())));
        }

        order.setTotal(total);
        return orderRepository.save(order);
    }

    public Page<Order> getOrders(UUID userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Order getOrder(UUID userId, UUID orderId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional
    public Order updateOrderStatus(UUID orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        order.setStatus(status);
        return orderRepository.save(order);
    }
}

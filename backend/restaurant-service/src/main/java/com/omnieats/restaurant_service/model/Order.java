package com.omnieats.restaurant_service.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID restaurantId;

    // Snapshot of restaurant name at time of order
    @Column(nullable = false)
    private String restaurantName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public Order(UUID userId, UUID restaurantId, String restaurantName, BigDecimal total) {
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.total = total;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    // --- Getters ---

    public UUID getId() { return id; }

    public UUID getUserId() { return userId; }

    public UUID getRestaurantId() { return restaurantId; }

    public String getRestaurantName() { return restaurantName; }

    public OrderStatus getStatus() { return status; }

    public BigDecimal getTotal() { return total; }

    public Instant getCreatedAt() { return createdAt; }

    public List<OrderItem> getItems() { return items; }

    // --- Setters ---

    public void setStatus(OrderStatus status) { this.status = status; }

    public void setTotal(BigDecimal total) { this.total = total; }
}

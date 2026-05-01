package com.omnieats.restaurant_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Snapshot of the menu item name at time of order
    @Column(nullable = false)
    private String menuItemName;

    @Column(nullable = false)
    private UUID menuItemId;

    // Snapshot of price at time of order
    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    public OrderItem() {}

    public OrderItem(String menuItemName, UUID menuItemId, BigDecimal price, Integer quantity, Order order) {
        this.menuItemName = menuItemName;
        this.menuItemId = menuItemId;
        this.price = price;
        this.quantity = quantity;
        this.order = order;
    }

    public UUID getId() { return id; }

    public String getMenuItemName() { return menuItemName; }

    public UUID getMenuItemId() { return menuItemId; }

    public BigDecimal getPrice() { return price; }

    public Integer getQuantity() { return quantity; }

    public Order getOrder() { return order; }

    public void setOrder(Order order) { this.order = order; }
}

package com.omnieats.restaurant_service.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private Double rating;

    private Integer deliveryTime;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "restaurant_cuisine_tags",
            joinColumns = @JoinColumn(name = "restaurant_id"),
            inverseJoinColumns = @JoinColumn(name = "cuisine_tag_id")
    )
    private List<CuisineTag> cuisineTags = new ArrayList<>();

    public Restaurant() {
    }

    public Restaurant(String name, String description, Double rating, Integer deliveryTime, List<CuisineTag> cuisineTags) {
        this.name = name;
        this.description = description;
        this.rating = rating;
        this.deliveryTime = deliveryTime;
        this.cuisineTags = cuisineTags;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(Integer deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public List<CuisineTag> getCuisineTags() {
        return cuisineTags;
    }

    public void setCuisineTags(List<CuisineTag> cuisineTags) {
        this.cuisineTags = cuisineTags;
    }
}

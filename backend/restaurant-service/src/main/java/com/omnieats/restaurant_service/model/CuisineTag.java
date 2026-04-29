package com.omnieats.restaurant_service.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "cuisine_tags")
public class CuisineTag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    public CuisineTag() {
    }

    public CuisineTag(String name) {
        this.name = name;
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
}

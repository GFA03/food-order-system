package com.omnieats.restaurant_service.controller;

import com.omnieats.restaurant_service.model.CuisineTag;
import com.omnieats.restaurant_service.service.CuisineTagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants/tags")
public class CuisineTagController {

    private final CuisineTagService cuisineTagService;

    public CuisineTagController(CuisineTagService cuisineTagService) {
        this.cuisineTagService = cuisineTagService;
    }

    @GetMapping
    public ResponseEntity<List<CuisineTag>> getAllTags() {
        return ResponseEntity.ok(cuisineTagService.getAllTags());
    }

    record CreateTagRequest(String name) {}

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CuisineTag> createTag(@RequestBody CreateTagRequest request) {
        CuisineTag tag = cuisineTagService.createTag(request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(tag);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        cuisineTagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}

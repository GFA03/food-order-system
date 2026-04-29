package com.omnieats.restaurant_service.service;

import com.omnieats.restaurant_service.model.CuisineTag;
import com.omnieats.restaurant_service.repository.CuisineTagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class CuisineTagService {

    private final CuisineTagRepository cuisineTagRepository;

    public CuisineTagService(CuisineTagRepository cuisineTagRepository) {
        this.cuisineTagRepository = cuisineTagRepository;
    }

    public List<CuisineTag> getAllTags() {
        return cuisineTagRepository.findAll();
    }

    public CuisineTag createTag(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tag name cannot be empty");
        }
        CuisineTag tag = new CuisineTag(name.trim());
        return cuisineTagRepository.save(tag);
    }

    public void deleteTag(UUID id) {
        if (!cuisineTagRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found");
        }
        cuisineTagRepository.deleteById(id);
    }
}

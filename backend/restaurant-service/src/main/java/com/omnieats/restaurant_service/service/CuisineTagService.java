package com.omnieats.restaurant_service.service;

import com.omnieats.restaurant_service.model.CuisineTag;
import com.omnieats.restaurant_service.repository.CuisineTagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class CuisineTagService {

    private static final Logger log = LoggerFactory.getLogger(CuisineTagService.class);

    private final CuisineTagRepository cuisineTagRepository;

    public CuisineTagService(CuisineTagRepository cuisineTagRepository) {
        this.cuisineTagRepository = cuisineTagRepository;
    }

    public List<CuisineTag> getAllTags() {
        log.debug("Fetching all cuisine tags");
        return cuisineTagRepository.findAll();
    }

    public CuisineTag createTag(String name) {
        if (name == null || name.trim().isEmpty()) {
            log.error("Tag creation failed — name is blank");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tag name cannot be empty");
        }
        CuisineTag tag = new CuisineTag(name.trim());
        CuisineTag saved = cuisineTagRepository.save(tag);
        log.info("Cuisine tag created: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    public void deleteTag(UUID id) {
        if (!cuisineTagRepository.existsById(id)) {
            log.error("Tag deletion failed — tag not found: id={}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found");
        }
        cuisineTagRepository.deleteById(id);
        log.info("Cuisine tag deleted: id={}", id);
    }
}

package com.omnieats.restaurant_service.service;

import com.omnieats.restaurant_service.exception.BadRequestException;
import com.omnieats.restaurant_service.exception.CuisineTagNotFoundException;
import com.omnieats.restaurant_service.model.CuisineTag;
import com.omnieats.restaurant_service.repository.CuisineTagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuisineTagServiceTest {

    @Mock private CuisineTagRepository cuisineTagRepository;
    @InjectMocks private CuisineTagService cuisineTagService;

    @Test
    void getAllTags_ReturnsList() {
        when(cuisineTagRepository.findAll()).thenReturn(List.of(new CuisineTag("Italian"), new CuisineTag("Vegan")));

        List<CuisineTag> result = cuisineTagService.getAllTags();

        assertEquals(2, result.size());
    }

    @Test
    void createTag_ValidName_Saves() {
        when(cuisineTagRepository.save(any(CuisineTag.class))).thenAnswer(i -> i.getArgument(0));

        CuisineTag result = cuisineTagService.createTag("Italian");

        assertEquals("Italian", result.getName());
        verify(cuisineTagRepository).save(any(CuisineTag.class));
    }

    @Test
    void createTag_TrimsWhitespace() {
        when(cuisineTagRepository.save(any(CuisineTag.class))).thenAnswer(i -> i.getArgument(0));

        CuisineTag result = cuisineTagService.createTag("  Vegan  ");

        assertEquals("Vegan", result.getName());
    }

    @Test
    void createTag_EmptyName_Throws400() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> cuisineTagService.createTag(""));
        assertEquals("Tag name cannot be empty", ex.getMessage());
    }

    @Test
    void createTag_BlankName_Throws400() {
        assertThrows(BadRequestException.class, () -> cuisineTagService.createTag("   "));
    }

    @Test
    void createTag_NullName_Throws400() {
        assertThrows(BadRequestException.class, () -> cuisineTagService.createTag(null));
    }

    @Test
    void deleteTag_Exists_Deletes() {
        UUID id = UUID.randomUUID();
        when(cuisineTagRepository.existsById(id)).thenReturn(true);

        cuisineTagService.deleteTag(id);

        verify(cuisineTagRepository).deleteById(id);
    }

    @Test
    void deleteTag_NotFound_Throws404() {
        UUID id = UUID.randomUUID();
        when(cuisineTagRepository.existsById(id)).thenReturn(false);

        assertThrows(CuisineTagNotFoundException.class, () -> cuisineTagService.deleteTag(id));
        verify(cuisineTagRepository, never()).deleteById(any());
    }
}

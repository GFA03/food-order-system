package com.omnieats.identity_service.repository;

import com.omnieats.identity_service.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_ShouldReturnUser_WhenExists() {
        // Arrange
        User user = new User("test@example.com", "Test User", "hash", List.of("USER"));
        userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
        assertEquals("Test User", foundUser.get().getName());
        assertEquals(1, foundUser.get().getRoles().size());
        assertEquals("USER", foundUser.get().getRoles().get(0));
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenDoesNotExist() {
        // Act
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertTrue(foundUser.isEmpty());
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenExists() {
        // Arrange
        User user = new User("exists@example.com", "Test User", "hash", List.of("USER"));
        userRepository.save(user);

        // Act
        boolean exists = userRepository.existsByEmail("exists@example.com");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenDoesNotExist() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertFalse(exists);
    }
}

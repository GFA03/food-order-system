package com.omnieats.identity_service.service;

import com.omnieats.identity_service.model.User;
import com.omnieats.identity_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.register("Test User", email, "password");
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("This email is already in use.", exception.getReason());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldSaveUser_WhenValid() {
        // Arrange
        String email = "test@example.com";
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        User savedUser = new User(email, "Test User", encodedPassword, List.of("USER"));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = authService.register("Test User", " " + email.toUpperCase() + " ", rawPassword);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userRepository, times(1)).save(argThat(user -> 
            user.getEmail().equals(email) && 
            user.getName().equals("Test User") && 
            user.getPasswordHash().equals(encodedPassword)
        ));
    }

    @Test
    void login_ShouldThrowException_WhenInvalidEmail() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.login(email, "password");
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid credentials", exception.getReason());
    }

    @Test
    void login_ShouldThrowException_WhenInvalidPassword() {
        // Arrange
        String email = "test@example.com";
        String rawPassword = "wrongPassword";
        User user = new User(email, "Test User", "correctHash", List.of("USER"));
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, "correctHash")).thenReturn(false);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.login(email, rawPassword);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid credentials", exception.getReason());
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {
        // Arrange
        String email = "test@example.com";
        String rawPassword = "correctPassword";
        User user = new User(email, "Test User", "correctHash", List.of("USER"));
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, "correctHash")).thenReturn(true);
        when(jwtService.issueToken(user)).thenReturn("mocked.jwt.token");

        // Act
        String token = authService.login(email, rawPassword);

        // Assert
        assertEquals("mocked.jwt.token", token);
        verify(jwtService, times(1)).issueToken(user);
    }
    
    @Test
    void loadByEmail_ShouldThrowException_WhenNotFound() {
        // Arrange
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.loadByEmail(email);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void loadByEmail_ShouldReturnUser_WhenFound() {
        // Arrange
        String email = "test@example.com";
        User user = new User(email, "Test User", "hash", List.of("USER"));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        User result = authService.loadByEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }
}

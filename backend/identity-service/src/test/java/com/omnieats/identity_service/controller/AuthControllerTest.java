package com.omnieats.identity_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnieats.identity_service.config.SecurityConfig;
import com.omnieats.identity_service.model.User;
import com.omnieats.identity_service.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser;

    @BeforeEach
    void setUp() throws Exception {
        mockUser = new User("test@example.com", "Test User", "hash", List.of("USER"));
        // Use reflection to set ID since it's auto-generated
        java.lang.reflect.Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(mockUser, UUID.randomUUID());
    }

    @Test
    void register_ShouldReturn201_WhenSuccessful() throws Exception {
        // Arrange
        AuthController.RegisterRequest request = new AuthController.RegisterRequest("Test User", "test@example.com", "password");
        
        when(authService.register(request.name(), request.email(), request.password())).thenReturn(mockUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(mockUser.getId().toString()))
                .andExpect(jsonPath("$.email").value(mockUser.getEmail()))
                .andExpect(jsonPath("$.name").value(mockUser.getName()));
    }

    @Test
    void login_ShouldReturn200AndToken_WhenCredentialsAreValid() throws Exception {
        // Arrange
        AuthController.LoginRequest request = new AuthController.LoginRequest("test@example.com", "password");
        String mockToken = "mock.jwt.token";

        when(authService.login(request.email(), request.password())).thenReturn(mockToken);
        when(authService.loadByEmail(request.email().trim().toLowerCase())).thenReturn(mockUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(mockToken))
                .andExpect(jsonPath("$.user.id").value(mockUser.getId().toString()))
                .andExpect(jsonPath("$.user.email").value(mockUser.getEmail()))
                .andExpect(jsonPath("$.user.roles[0]").value("USER"));
    }
}

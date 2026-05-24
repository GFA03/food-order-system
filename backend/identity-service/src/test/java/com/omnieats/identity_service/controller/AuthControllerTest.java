package com.omnieats.identity_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnieats.identity_service.config.SecurityConfig;
import com.omnieats.identity_service.exception.EmailAlreadyInUseException;
import com.omnieats.identity_service.exception.InvalidCredentialsException;
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

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
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
        AuthController.LoginRequest request = new AuthController.LoginRequest("test@example.com", "password", null);
        String mockToken = "mock.jwt.token";

        when(authService.login(request.email(), request.password(), false)).thenReturn(mockToken);
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

    @Test
    void login_ShouldPassRememberMeTrue_WhenFlagSet() throws Exception {
        // Arrange
        AuthController.LoginRequest request = new AuthController.LoginRequest("test@example.com", "password", true);
        String mockToken = "long.lived.token";

        when(authService.login(request.email(), request.password(), true)).thenReturn(mockToken);
        when(authService.loadByEmail(request.email().trim().toLowerCase())).thenReturn(mockUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(mockToken));

        verify(authService).login(request.email(), request.password(), true);
    }

    // ── Bean Validation + global exception handling ──────────────────────────────

    @Test
    void register_InvalidBody_Returns400() throws Exception {
        // blank name, malformed email, too-short password → MethodArgumentNotValidException → 400
        String body = "{\"name\":\"\",\"email\":\"not-an-email\",\"password\":\"123\"}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void register_DuplicateEmail_Returns409() throws Exception {
        when(authService.register(anyString(), anyString(), anyString()))
                .thenThrow(new EmailAlreadyInUseException("This email is already in use."));

        String body = "{\"name\":\"Test User\",\"email\":\"test@example.com\",\"password\":\"password\"}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void login_BadCredentials_Returns401() throws Exception {
        when(authService.login(anyString(), anyString(), anyBoolean()))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        String body = "{\"email\":\"test@example.com\",\"password\":\"wrongpass\"}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}

package com.omnieats.identity_service.controller;

import com.omnieats.identity_service.model.User;
import com.omnieats.identity_service.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ── POST /api/auth/register ───────────────────────────────────────────────

    record RegisterRequest(String name, String email, String password) {}

    record RegisterResponse(String id, String email, String name) {}

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        User user = authService.register(request.name(), request.email(), request.password());
        RegisterResponse body = new RegisterResponse(
                user.getId().toString(),
                user.getEmail(),
                user.getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    // ── POST /api/auth/login ──────────────────────────────────────────────────

    record LoginRequest(String email, String password) {}

    /**
     * AuthResponse shape must match what the frontend's AuthContext.tsx expects:
     * {@code { token: string, user: { id, email, name, roles[] } }}
     */
    record AuthUserDto(String id, String email, String name, List<String> roles) {}

    record AuthResponse(String token, AuthUserDto user) {}

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // authService.login throws 401 ResponseStatusException on bad credentials
        String token = authService.login(request.email(), request.password());

        // Re-fetch the user to build the response DTO (token already validated internally)
        User user = authService.loadByEmail(request.email().trim().toLowerCase());

        AuthUserDto userDto = new AuthUserDto(
                user.getId().toString(),
                user.getEmail(),
                user.getName(),
                user.getRoles()
        );

        return ResponseEntity.ok(new AuthResponse(token, userDto));
    }
}

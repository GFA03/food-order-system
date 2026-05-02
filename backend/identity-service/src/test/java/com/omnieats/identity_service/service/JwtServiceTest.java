package com.omnieats.identity_service.service;

import com.omnieats.identity_service.config.JwtProperties;
import com.omnieats.identity_service.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private String secret = "this-is-a-very-secure-test-secret-key-that-is-long-enough";
    private long expirationMs = 3600000;         // 1 hour
    private long rememberMeExpirationMs = 7200000; // 2 hours

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);
        properties.setExpirationMs(expirationMs);
        properties.setRememberMeExpirationMs(rememberMeExpirationMs);
        jwtService = new JwtService(properties);
    }

    private User buildUser() {
        User user = new User("test@example.com", "Test User", "hash", List.of("USER"));
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    @Test
    void issueToken_ShouldContainCorrectClaims() {
        User user = buildUser();

        String token = jwtService.issueToken(user);

        assertNotNull(token);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(user.getId().toString(), claims.getSubject());
        assertEquals(user.getEmail(), claims.get("email"));
        assertEquals(user.getName(), claims.get("name"));

        List<String> roles = claims.get("roles", List.class);
        assertNotNull(roles);
        assertTrue(roles.contains("USER"));
    }

    @Test
    void issueToken_WithRememberMe_UsesLongerExpiration() {
        User user = buildUser();

        String normalToken = jwtService.issueToken(user, false);
        String rememberToken = jwtService.issueToken(user, true);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        Claims normalClaims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(normalToken).getPayload();
        Claims rememberClaims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(rememberToken).getPayload();

        assertTrue(rememberClaims.getExpiration().after(normalClaims.getExpiration()),
                "remember-me token must expire later than the normal token");
    }
}

package com.omnieats.gateway_service.security;

import com.omnieats.gateway_service.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtValidatorTest {

    private static final String SECRET = "omnieats-test-secret-key-must-be-at-least-32-chars";
    private JwtValidator jwtValidator;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        jwtValidator = new JwtValidator(props);
        signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void validToken_returnsClaims() {
        String token = Jwts.builder()
                .subject("user-123")
                .claim("roles", List.of("USER"))
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey)
                .compact();

        Claims claims = jwtValidator.validate(token);

        assertThat(claims.getSubject()).isEqualTo("user-123");
        assertThat(claims.get("roles")).isNotNull();
    }

    @Test
    void expiredToken_throwsJwtException() {
        String token = Jwts.builder()
                .subject("user-123")
                .expiration(new Date(System.currentTimeMillis() - 60_000)) // already expired
                .signWith(signingKey)
                .compact();

        assertThatThrownBy(() -> jwtValidator.validate(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void tamperedSignature_throwsJwtException() {
        String token = Jwts.builder()
                .subject("user-123")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey)
                .compact();

        // Tamper with the signature (last segment)
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1] + ".invalidsignature";

        assertThatThrownBy(() -> jwtValidator.validate(tampered))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void wrongSecret_throwsJwtException() {
        SecretKey wrongKey = Keys.hmacShaKeyFor(
                "completely-different-secret-key-32chars!!".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("user-123")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(wrongKey)
                .compact();

        assertThatThrownBy(() -> jwtValidator.validate(token))
                .isInstanceOf(JwtException.class);
    }
}

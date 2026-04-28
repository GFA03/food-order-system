package com.omnieats.identity_service.service;

import com.omnieats.identity_service.config.JwtProperties;
import com.omnieats.identity_service.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Issues signed JWTs that can be validated by the gateway-service's JwtValidator.
 *
 * <p>Claims layout (must match what AuthContext.tsx decodes on the frontend):
 * <ul>
 *   <li>{@code sub}   — user UUID (string)</li>
 *   <li>{@code email} — user email</li>
 *   <li>{@code name}  — display name</li>
 *   <li>{@code roles} — list of role strings, e.g. ["USER"]</li>
 * </ul>
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(JwtProperties jwtProperties) {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = jwtProperties.getExpirationMs();
    }

    public String issueToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("roles", user.getRoles())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }
}

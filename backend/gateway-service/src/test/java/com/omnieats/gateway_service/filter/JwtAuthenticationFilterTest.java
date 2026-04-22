package com.omnieats.gateway_service.filter;

import com.omnieats.gateway_service.config.JwtProperties;
import com.omnieats.gateway_service.security.JwtValidator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "omnieats-test-secret-key-must-be-at-least-32-chars";
    private JwtAuthenticationFilter filter;
    private SecretKey signingKey;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        JwtValidator validator = new JwtValidator(props);
        filter = new JwtAuthenticationFilter(validator);
        signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void allowlistedPath_login_passesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/auth/login")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        // Chain was called (no 401 set)
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void allowlistedPath_register_passesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/auth/register")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void allowlistedPath_actuator_passesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/actuator/health")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void missingToken_returns401() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/restaurants")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void invalidToken_returns401() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/restaurants")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not.a.valid.token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void validToken_passesThrough() {
        String token = Jwts.builder()
                .subject("user-42")
                .claim("roles", List.of("USER"))
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey)
                .compact();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/restaurants")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        // Should not be 401
        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }
}

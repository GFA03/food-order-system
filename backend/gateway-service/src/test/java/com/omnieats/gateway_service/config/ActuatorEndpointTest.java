package com.omnieats.gateway_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Verifies that the Spring Boot Actuator endpoints are accessible.
 * The /actuator/prometheus endpoint requires the full Prometheus registry
 * to be active; this test verifies /actuator/health which is always available.
 * Prometheus metrics are verified at runtime when the full stack is running.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ActuatorEndpointTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void actuatorHealth_returns200() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void actuatorBase_returns200() {
        webTestClient.get()
                .uri("/actuator")
                .exchange()
                .expectStatus().isOk();
    }
}

package com.omnieats.ai_aggregator_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * {@link RestClient} pointed at restaurant-service. The base URL is profile-driven
 * ({@code restaurant-service.base-url}) — this is the project's lightweight,
 * registry-free service discovery.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restaurantRestClient(RestClient.Builder builder,
                                           @Value("${restaurant-service.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}

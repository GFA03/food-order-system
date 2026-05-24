package com.omnieats.ai_aggregator_service.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds the {@link ChatClient} from the Spring AI auto-configured builder (backed by the
 * Ollama {@code ChatModel}). Kept as an explicit bean so tests can swap the underlying model.
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}

package com.omnieats.ai_aggregator_service;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class AiAggregatorServiceApplicationTests {

	// Replace the Ollama-backed ChatModel so the context loads without a running LLM.
	@MockitoBean
	private ChatModel chatModel;

	@Test
	void contextLoads() {
	}

}

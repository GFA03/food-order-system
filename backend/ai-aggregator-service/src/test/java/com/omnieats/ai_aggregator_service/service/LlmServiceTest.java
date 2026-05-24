package com.omnieats.ai_aggregator_service.service;

import com.omnieats.ai_aggregator_service.dto.SearchFilter;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LlmServiceTest {

    @Test
    void extractFilter_delegatesToChatClientStructuredOutput() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        SearchFilter expected = new SearchFilter(List.of("ramen"), null, null, null, true);
        when(chatClient.prompt().system(anyString()).user(anyString()).call().entity(SearchFilter.class))
                .thenReturn(expected);

        LlmService llmService = new LlmService(chatClient);

        assertThat(llmService.extractFilter("spicy ramen")).isEqualTo(expected);
    }
}

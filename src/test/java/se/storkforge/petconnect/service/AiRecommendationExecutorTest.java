package se.storkforge.petconnect.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import se.storkforge.petconnect.service.aiService.AiRecommendationExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AiRecommendationExecutorTest {

    private AiRecommendationExecutor executor;

    @BeforeEach
    void setUp() {
        ChatClient mockChatClient = mock(ChatClient.class);
        executor = new AiRecommendationExecutor(mockChatClient);
    }

    @Test
    void fallback_ShouldReturnFallbackMessage() {
        Prompt prompt = new Prompt("Recommend a pet based on user data.");
        RuntimeException ex = new RuntimeException("AI failed");

        String result = executor.fallback(ex, prompt);

        assertEquals("Our recommendation engine is currently unavailable. Please try again later.", result);
    }
}
package se.storkforge.petconnect.service.aiService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;

@Service
public class AiRecommendationExecutor {

    private static final Logger logger = LoggerFactory.getLogger(AiRecommendationExecutor.class);
    private final ChatClient chatClient;

    public AiRecommendationExecutor(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String callAi(Prompt prompt) {
        return chatClient.prompt(prompt).call().content();
    }

    @Recover
    public String fallback(RuntimeException e, Prompt prompt) {
        logger.error("AI call failed after retries", e);
        return "Our recommendation engine is currently unavailable. Please try again later.";
    }
}
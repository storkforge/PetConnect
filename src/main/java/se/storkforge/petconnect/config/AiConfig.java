package se.storkforge.petconnect.config;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${spring.ai.openai.api-key:}")
    private String openAiApiKey;

    @PostConstruct
    public void validateApiKey() {
        if (openAiApiKey == null || openAiApiKey.trim().isEmpty()) {
            throw new IllegalStateException("Missing required OpenAI API key. Please set the OPENAI_API_KEY environment variable.");
        }
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .build();
    }
}
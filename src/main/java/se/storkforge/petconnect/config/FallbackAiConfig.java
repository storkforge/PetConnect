package se.storkforge.petconnect.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;

import java.util.List;

@Configuration
public class FallbackAiConfig {

    @Bean
    @Profile("!prod") // Or a specific profile for fallback
    public ChatModel fallbackChatModel() {
        return new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                String userMessage = prompt.getContents();

                // Simple rule-based logic
                String responseContent;
                if (userMessage.contains("recommendation")) {
                    responseContent = "Fallback: Based on basic matching, we suggest a young cat or dog.";
                } else if (userMessage.contains("care tip")) {
                    responseContent = "Fallback: Regular vet check-ups are important for all pets.";
                } else {
                    responseContent = "Fallback: I'm currently limited in my responses. Please try again later.";
                }

                // Create proper Generation with AssistantMessage
                Generation generation = new Generation(new AssistantMessage(responseContent));
                return new ChatResponse(List.of(generation));
            }

            @Override
            public Flux<ChatResponse> stream(Prompt prompt) {
                // For streaming, we just wrap the regular call in a Flux
                return Flux.just(call(prompt));
            }
        };
    }
}
package se.storkforge.petconnect.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    private String openAiApiKey;

    @PostConstruct
    public void loadEnvAndValidateApiKey() {
        Dotenv dotenv = Dotenv.configure()
                .filename("OpenAI.env")  // make sure the filename matches and is in your project root
                .load();

        openAiApiKey = dotenv.get("OPENAI_API_KEY");

        if (openAiApiKey == null || openAiApiKey.trim().isEmpty()) {
            throw new IllegalStateException("Missing required OpenAI API key in OpenAI.env file.");
        }
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    // exposes the key if needed elsewhere in our proj
    public String getOpenAiApiKey() {
        return openAiApiKey;
    }
}
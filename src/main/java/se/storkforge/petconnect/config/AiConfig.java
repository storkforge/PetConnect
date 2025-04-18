package se.storkforge.petconnect.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${OPENAI_API_KEY}")
    private String openAiApiKey;

    @PostConstruct
    public void loadEnvAndValidateApiKey() {
        Dotenv dotenv = Dotenv.configure()
                .filename("OpenAI.env")  // make sure the env filename matches and is in your project root
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

    public String getOpenAiApiKey() {
        return openAiApiKey;
    }
}
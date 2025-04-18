package se.storkforge.petconnect.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Configuration
@EnableRetry
public class AiConfig {
    private static final Logger logger = LoggerFactory.getLogger(AiConfig.class);

    @Value("${spring.ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${spring.ai.openai.model:gpt-3.5-turbo}")
    private String model;

    @Value("${spring.ai.openai.temperature:0.7}")
    private Double temperature;

    @Value("${spring.ai.openai.max-tokens:200}")
    private Integer maxTokens;

    @Value("${spring.ai.system-message:You are PetConnect AI, a helpful pet care assistant}")
    private String systemMessage;

    @Value("${spring.retry.initial-interval:1000}")
    private long initialInterval;

    @Value("${spring.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${spring.ai.openai.timeout:30s}")
    private Duration timeout;

    @PostConstruct
    public void validateConfig() {
        if (!StringUtils.hasText(openAiApiKey)) {
            throw new IllegalStateException("OpenAI API key is missing");
        }
        if (temperature < 0 || temperature > 2) {
            throw new IllegalStateException("Temperature must be between 0 and 2");
        }
        if (maxTokens < 50 || maxTokens > 4096) {  // Considering typical model limits
            throw new IllegalStateException("Max tokens must be between 50 and 4096");
        }
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            throw new IllegalStateException("Timeout must be a positive duration");
        }
        logger.info("AI configuration validated");
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(openAiChatOptions())
                .defaultSystem(systemMessage)
                .build();
    }

    @Bean
    public OpenAiChatOptions openAiChatOptions() {
        return OpenAiChatOptions.builder()
                .model(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
    }

    @Bean
    public RetryTemplate aiRetryTemplate() {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(10000);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxAttempts);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
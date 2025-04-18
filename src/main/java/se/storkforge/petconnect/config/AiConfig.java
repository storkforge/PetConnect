package se.storkforge.petconnect.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.List;

@Configuration
@EnableRetry
public class AiConfig {
    private static final Logger logger = LoggerFactory.getLogger(AiConfig.class);

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Value("${spring.ai.openai.chat.options.model:gpt-3.5-turbo}")
    private String model;

    @Value("${spring.ai.openai.chat.options.temperature:0.7}")
    private Double temperature;

    @Value("${spring.ai.openai.chat.options.max-tokens:100}")
    private Integer maxTokens;

    @Value("${petconnect.ai.system-message:You are PetConnect AI, a helpful pet care assistant}")
    private String systemMessage;

    @Value("${petconnect.ai.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${petconnect.ai.retry.initial-interval:1000}")
    private long initialInterval;

    @Value("${petconnect.ai.retry.multiplier:2}")
    private double multiplier;

    @Value("${petconnect.ai.retry.max-interval:10000}")
    private long maxInterval;

    @Value("${petconnect.ai.fallback.enabled:false}")
    private boolean fallbackEnabled;

    @PostConstruct
    public void validateConfig() {
        if (!StringUtils.hasText(openAiApiKey)) {
            throw new IllegalStateException("OpenAI API key is missing");
        }
        if (temperature < 0 || temperature > 2) {
            throw new IllegalStateException("Temperature must be between 0 and 2");
        }
        if (maxTokens < 1 || maxTokens > 4096) {
            throw new IllegalStateException("Max tokens must be between 1 and 4096");
        }
        logger.info("AI configuration validated");
    }

    @Primary
    @Bean
    public OpenAiChatModel openAiChatModel(OpenAiChatOptions openAiChatOptions) {
        return OpenAiChatModel.builder()
                .defaultOptions(openAiChatOptions)
                .build();
    }
    @Bean
    public ChatClient chatClient(
            @Qualifier("openAiChatModel") ChatModel chatModel,
            @Autowired(required = false) @Qualifier("fallbackChatModel") ChatModel fallbackModel) {

        ChatModel modelToUse = fallbackEnabled && fallbackModel != null ?
                new RetryableChatModel(chatModel, fallbackModel, maxAttempts) :
                chatModel;

        return ChatClient.builder(modelToUse)
                .defaultOptions(openAiChatOptions())
                .defaultSystem(systemMessage)
                .build();
    }

    @Bean
    public OpenAiChatOptions openAiChatOptions() {
        OpenAiChatOptions options = new OpenAiChatOptions();
        options.setModel(model);
        options.setTemperature(temperature);
        options.setMaxTokens(maxTokens);
        return options;
    }

    @Bean
    public RetryTemplate aiRetryTemplate() {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMultiplier(multiplier);
        backOffPolicy.setMaxInterval(maxInterval);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxAttempts);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    @Bean
    @ConditionalOnProperty(name = "petconnect.ai.fallback.enabled", havingValue = "true")
    public ChatModel fallbackChatModel() {
        return new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                return new ChatResponse(List.of(
                        new Generation(new AssistantMessage("Our AI service is temporarily unavailable. Please try again later."))
                ));
            }

            @Override
            public Flux<ChatResponse> stream(Prompt prompt) {
                return Flux.just(call(prompt));
            }
        };
    }

    private record RetryableChatModel(ChatModel primary, ChatModel fallback, int maxAttempts) implements ChatModel {

        @Override
            public ChatResponse call(Prompt prompt) {
                for (int attempt = 0; attempt < maxAttempts; attempt++) {
                    try {
                        return primary.call(prompt);
                    } catch (Exception e) {
                        if (attempt == maxAttempts - 1) {
                            return fallback.call(prompt);
                        }
                    }
                }
                return fallback.call(prompt);
            }

            @Override
            public Flux<ChatResponse> stream(Prompt prompt) {
                return primary.stream(prompt).onErrorResume(e -> fallback.stream(prompt));
            }
        }
}
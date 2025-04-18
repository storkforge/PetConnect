package se.storkforge.petconnect.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import reactor.core.publisher.Flux;

public class RetryableChatModel implements ChatModel {
    private final ChatModel primaryModel;
    private final ChatModel fallbackModel;
    private final RetryTemplate retryTemplate;
    private final int maxAttempts;

    public RetryableChatModel(ChatModel primaryModel,
                              ChatModel fallbackModel,
                              RetryTemplate retryTemplate,
                              int maxAttempts) {
        this.primaryModel = primaryModel;
        this.fallbackModel = fallbackModel;
        this.retryTemplate = retryTemplate;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        return retryTemplate.execute(context -> {
            try {
                return primaryModel.call(prompt);
            } catch (Exception e) {
                if (shouldUseFallback(context)) {
                    return fallbackModel.call(prompt);
                }
                throw e;
            }
        });
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return retryTemplate.execute(context -> {
            try {
                return primaryModel.stream(prompt);
            } catch (Exception e) {
                if (shouldUseFallback(context)) {
                    return fallbackModel.stream(prompt);
                }
                throw e;
            }
        });
    }

    private boolean shouldUseFallback(RetryContext context) {
        // Last attempt is maxAttempts-1 because attempts are 0-based
        return context.getRetryCount() >= maxAttempts - 1;
    }
}
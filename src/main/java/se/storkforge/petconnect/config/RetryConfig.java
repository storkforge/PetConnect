package se.storkforge.petconnect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.policy.SimpleRetryPolicy;

@Configuration
@EnableRetry
public class RetryConfig {

    @Bean
    public SimpleRetryPolicy simpleRetryPolicy() {
        SimpleRetryPolicy policy = new SimpleRetryPolicy();
        policy.setMaxAttempts(3); // Set your desired maximum number of attempts here
        return policy;
    }
}
package se.storkforge.petconnect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.MethodInvocationRecoverer;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class DisableAiRetryConfig {

    /**
     * Override Spring AI's default RetryOperationsInterceptor to disable retries entirely.
     */
    @Bean
    public RetryOperationsInterceptor retryOperationsInterceptor() {
        return new RetryOperationsInterceptor() {
            @Override
            public Object invoke(org.aopalliance.intercept.MethodInvocation invocation) throws Throwable {
                // Skip retry logic and proceed directly
                return invocation.proceed();
            }

            @Override
            public void setRecoverer(MethodInvocationRecoverer<?> recoverer) {
                // No-op, disabling retry fallback
            }
        };
    }
}

package se.storkforge.petconnect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile({"dev", "test"})
@Configuration
public class MockTwilioConfig {

    @Bean
    public TwilioConfig mockTwilio() {
        return new TwilioConfig("mockSid", "mockToken", "+1234567890");
    }
}

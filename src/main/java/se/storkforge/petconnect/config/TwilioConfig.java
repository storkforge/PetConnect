package se.storkforge.petconnect.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!dev")
public class TwilioConfig {

    private final String accountSid;
    private final String authToken;
    private final String fromPhoneNumber;

    public TwilioConfig(
            @Value("${twilio.account-sid}") String accountSid,
            @Value("${twilio.auth-token}") String authToken,
            @Value("${twilio.phone-number}") String fromPhoneNumber) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromPhoneNumber = fromPhoneNumber;
    }

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    @Bean
    public String twilioAccountSid() {
        return accountSid;
    }

    @Bean
    public String twilioAuthToken() {
        return authToken;
    }

    @Bean
    public String twilioPhoneNumber() {
        return fromPhoneNumber;
    }

    public String getFromPhoneNumber() {
        return fromPhoneNumber;
    }
}
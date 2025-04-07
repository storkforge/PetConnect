package se.storkforge.petconnect.service;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import com.twilio.type.PhoneNumber;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${twilio.account_sid}")
    private String accountSid;

    @Value("${twilio.auth_token}")
    private String authToken;

    @Value("${twilio.phone_number}")
    private String fromPhoneNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void sendSms(String to, String messageText) {
        if (to == null || to.isEmpty()) {
            throw new IllegalArgumentException("Recipient phone number cannot be empty");
        }

        try {
            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(fromPhoneNumber),
                    messageText

            ).create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS notification", e);
        }

    }
}

package se.storkforge.petconnect.service;

import org.springframework.stereotype.Service;
import se.storkforge.petconnect.config.TwilioConfig;

@Service
public class SmsService {

    private final TwilioConfig twilioConfig;
    private final SmsSender smsSender;

    public SmsService(TwilioConfig twilioConfig, SmsSender smsSender) {
        this.twilioConfig = twilioConfig;
        this.smsSender = smsSender;
    }

    /**
     * Sends an SMS message to a specified phone number.
     *
     * @param to          - recipient's phone number.
     * @param messageText - content of the SMS message.
     */
    public void sendSms(String to, String messageText) {
        if (to == null || to.isEmpty()) {
            throw new IllegalArgumentException("Recipient phone number cannot be empty");
        }

        try {
            smsSender.send(to, twilioConfig.getFromPhoneNumber(), messageText);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS notification", e);
        }

    }
}

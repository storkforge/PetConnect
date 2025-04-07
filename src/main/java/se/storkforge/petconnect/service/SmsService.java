package se.storkforge.petconnect.service;

import com.twilio.type.PhoneNumber;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.storkforge.petconnect.config.TwilioConfig;

@Service
public class SmsService {

   @Autowired
   private TwilioConfig twilioConfig;

    public void sendSms(String to, String messageText) {
        if (to == null || to.isEmpty()) {
            throw new IllegalArgumentException("Recipient phone number cannot be empty");
        }

        try {
            Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(twilioConfig.getFromPhoneNumber()),
                    messageText

            ).create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS notification", e);
        }

    }
}

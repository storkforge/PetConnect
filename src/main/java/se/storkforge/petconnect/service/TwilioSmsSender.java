package se.storkforge.petconnect.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Component;

@Component
public class TwilioSmsSender implements SmsSender {

    @Override
    public void send(String to, String from, String message) {
        Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(from),
                message
        ).create();
    }
}

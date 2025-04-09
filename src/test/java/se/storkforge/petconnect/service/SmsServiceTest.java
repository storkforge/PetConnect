package se.storkforge.petconnect.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.storkforge.petconnect.config.TwilioConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {
    @Mock
    private TwilioConfig twilioConfig;

    @InjectMocks
    private SmsService smsService;

    @Mock
    private SmsSender smsSender;

    @Test
    void sendSms_validInput_shouldSendSuccessfully() {

        String to = "+762373333";
        String message = "Hello from test!";
        String from = "+762374444";

        when(twilioConfig.getFromPhoneNumber()).thenReturn(from);

        assertDoesNotThrow(() ->
                smsService.sendSms(to, message)
        );

        verify(smsSender).send(to, from, message);
    }

    @Test
    void sendSms_invalidInput_shouldThrowException() {

        String to = "";
        String message = "Message";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> smsService.sendSms(to, message)
        );

        assertEquals("Recipient phone number cannot be empty",exception.getMessage());
        verifyNoInteractions(smsSender);

    }

    @Test
    void sendSms_senderThrows_shouldWrapInRuntimeException() {

        String to = "+762373333";
        String message = "Hello again!";
        String from = "+762374444";

        when(twilioConfig.getFromPhoneNumber()).thenReturn(from);
        doThrow(new RuntimeException("Twilio error")).when(smsSender).send(to, from, message);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> smsService.sendSms(to, message)
        );

        assertTrue(exception.getMessage().contains("Failed to send SMS notification") );
    }
  
}

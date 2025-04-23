package se.storkforge.petconnect.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailService mailService;

    @Test
    void sendMeetUpNotification_validEmail_shouldSendMail() {

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        mailService.sendMeetUpNotification("test@example.com", "MeetUp Reminder", "Don't forget the meet-up!");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}

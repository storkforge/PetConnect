package se.storkforge.petconnect.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.storkforge.petconnect.service.MailService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MailControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private MailService mailService;

    @InjectMocks
    private MailController mailController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mailController).build();
    }

    @Test
    void testSendMailWithDefaultValues() throws Exception {
        // Given
        String to = "test@example.com";
        String defaultSubject = "MeetUp Reminder";
        String defaultContent = "Don't forget the meet-up!";

        doNothing().when(mailService).sendMeetUpNotification(to, defaultSubject, defaultContent);

        // When & Then
        mockMvc.perform(post("/mail/send")
                        .param("to", to))
                .andExpect(status().isOk())
                .andExpect(content().string("Mail sent to " + to));

        verify(mailService).sendMeetUpNotification(to, defaultSubject, defaultContent);
    }

    @Test
    void testSendMailWithCustomValues() throws Exception {
        // Given
        String to = "custom@example.com";
        String subject = "Urgent: Meeting Update";
        String content = "The meeting has been rescheduled!";

        doNothing().when(mailService).sendMeetUpNotification(to, subject, content);

        // When & Then
        mockMvc.perform(post("/mail/send")
                        .param("to", to)
                        .param("subject", subject)
                        .param("content", content))
                .andExpect(status().isOk())
                .andExpect(content().string("Mail sent to " + to));

        verify(mailService).sendMeetUpNotification(to, subject, content);
    }

    @Test
    void testSendMailWithPartialCustomValues() throws Exception {
        // Given
        String to = "partial@example.com";
        String customSubject = "Custom Subject Only";
        String defaultContent = "Don't forget the meet-up!";

        doNothing().when(mailService).sendMeetUpNotification(to, customSubject, defaultContent);

        // When & Then
        mockMvc.perform(post("/mail/send")
                        .param("to", to)
                        .param("subject", customSubject))
                .andExpect(status().isOk())
                .andExpect(content().string("Mail sent to " + to));

        verify(mailService).sendMeetUpNotification(to, customSubject, defaultContent);
    }

    @Test
    void testSendMailWithInvalidEmail() throws Exception {
        // When & Then
        mockMvc.perform(post("/mail/send")
                        .param("to", "invalid-email"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(mailService);
    }

    @Test
    void testSendMailWhenServiceThrowsException() throws Exception {
        // Given
        String to = "error@example.com";
        String errorMessage = "SMTP server unavailable";

        doThrow(new RuntimeException(errorMessage))
                .when(mailService)
                .sendMeetUpNotification(to, "MeetUp Reminder", "Don't forget the meet-up!");

        // When & Then
        mockMvc.perform(post("/mail/send")
                        .param("to", to))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to send email: " + errorMessage));

        verify(mailService).sendMeetUpNotification(
                to,
                "MeetUp Reminder",
                "Don't forget the meet-up!"
        );
    }
}
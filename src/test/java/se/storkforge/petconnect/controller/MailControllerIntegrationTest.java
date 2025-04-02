package se.storkforge.petconnect.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.storkforge.petconnect.service.MailService;


import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MailControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private MailService mailService;

    @InjectMocks
    private MailController mailController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(mailController).build();
    }

    @Test
    void testSendMailEndpoint() throws Exception {
        doNothing().when(mailService).sendMeetUpNotification(eq("text@example.com"), anyString(), anyString());

        mockMvc.perform(get("/mail/send")
                .param("to", "text@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Mail sent to text@example.com"));

        verify(mailService, times(1)).sendMeetUpNotification(eq("text@example.com"), anyString(), anyString());
    }

}

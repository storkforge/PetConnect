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
import se.storkforge.petconnect.service.SmsService;


import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SmsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private SmsController smsController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(smsController).build();
    }

    @Test
    public void sendSms_validRequest_shouldReturnOk() throws Exception {
        String to = "+46762378510";
        String message = "Don't forget the meet-up!";

        mockMvc.perform(post("/sms/send")
                .param("to", to)
                .param("message", message))
                .andExpect(status().isOk())
                .andExpect(content().string("SMS sent to " + to));

        verify(smsService).sendSms(to, message);

    }

    @Test
    void sendSms_missingParam_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/sms/send"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendSms_serviceThrows_shouldReturnServerError() throws Exception {
        doThrow(new RuntimeException("SMS error"))
                .when(smsService).sendSms(anyString(), anyString());

        mockMvc.perform(post("/sms/send")
        .param("to", "+46762378510")
                .param("message", "Test message from SmsController"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to send SMS: SMS error"));
    }

}

package se.storkforge.petconnect.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import se.storkforge.petconnect.exception.SmsControllerExceptionHandler;
import se.storkforge.petconnect.service.SmsService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SmsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private SmsController smsController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        ExceptionHandlerExceptionResolver exceptionResolver = new ExceptionHandlerExceptionResolver();
        exceptionResolver.setApplicationContext(null); // You might need to provide an ApplicationContext if it's null
        exceptionResolver.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(smsController)
                .setValidator(validator)
                .setHandlerExceptionResolvers(exceptionResolver)
                .build();
    }

    @Test
    void sendSms_validRequest_shouldReturnOk() throws Exception {
        String validPhone = "+46762373333";
        String message = "Test message";

        mockMvc.perform(post("/sms/send")
                        .param("to", validPhone)
                        .param("content", message))
                .andExpect(status().isOk())
                .andExpect(content().string("SMS sent to " + validPhone));

        verify(smsService).sendSms(validPhone, message);
    }

    @Test
    void sendSms_emptyPhoneNumber_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/sms/send")
                        .param("to", "")
                        .param("content", "Test message"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Phone number cannot be empty"));
    }

    @Test
    void sendSms_invalidPhoneFormat_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/sms/send")
                        .param("to", "abc123")
                        .param("content", "Test message"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid phone number format"));
    }

    @Test
    void sendSms_missingPhoneNumber_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/sms/send")
                        .param("content", "Test message"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendSms_serviceThrowsException_shouldReturnServerError() throws Exception {
        String validPhone = "+46762373333";
        doThrow(new RuntimeException("Service error"))
                .when(smsService).sendSms(validPhone, "Test message");

        mockMvc.perform(post("/sms/send")
                        .param("to", validPhone)
                        .param("content", "Test message"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Failed to send SMS")));
    }

    @Test
    void sendSms_missingMessage_shouldUseDefault() throws Exception {
        String validPhone = "+46762373333";

        mockMvc.perform(post("/sms/send")
                        .param("to", validPhone))
                .andExpect(status().isOk());

        verify(smsService).sendSms(validPhone, "Don't forget the meet-up!");
    }
}
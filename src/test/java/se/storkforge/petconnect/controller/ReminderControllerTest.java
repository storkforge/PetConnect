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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import se.storkforge.petconnect.dto.ReminderInputDTO;
import se.storkforge.petconnect.dto.ReminderResponseDTO;
import se.storkforge.petconnect.service.ReminderService;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReminderControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private ReminderService reminderService;

    @InjectMocks
    private ReminderController reminderController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reminderController).build();
    }

    @Test
    void createReminder_ShouldReturnCreatedStatus() throws Exception {
        ReminderInputDTO inputDTO = new ReminderInputDTO();
        inputDTO.setPetId(1L);
        inputDTO.setTitle("Vet Appointment");
        inputDTO.setType("Medical");
        inputDTO.setScheduledDate(LocalDateTime.now().plusDays(1));

        // Simulate an authenticated user
        Principal mockPrincipal = () -> "testUser"; // Replace "testUser" with the desired username

        mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO))
                        .principal(mockPrincipal)) // Add the mock Principal
                .andExpect(status().isCreated());

        verify(reminderService).createReminder(any(ReminderInputDTO.class), eq("testUser")); // Verify with the expected username
    }

    @Test
    void getUpcomingReminders_ShouldReturnOkStatusAndList() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(7);
        ReminderResponseDTO responseDTO = new ReminderResponseDTO();
        responseDTO.setTitle("Upcoming Reminder");
        List<ReminderResponseDTO> reminders = Collections.singletonList(responseDTO);

        when(reminderService.getUpcomingReminders("testUser", now, future))
                .thenReturn(reminders);

        mockMvc.perform(get("/api/reminders/upcoming")
                        .principal(() -> "testUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Upcoming Reminder"));
    }

    @Test
    void deleteReminder_ShouldReturnNoContentStatus() throws Exception {
        Long reminderId = 1L;

        mockMvc.perform(delete("/api/reminders/{id}", reminderId)
                        .principal(() -> "testUser"))
                .andExpect(status().isNoContent());

        verify(reminderService).deleteReminder(reminderId, "testUser");
    }

    @Test
    void createReminder_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        ReminderInputDTO inputDTO = new ReminderInputDTO(); // Missing required fields

        mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isBadRequest());
    }
}
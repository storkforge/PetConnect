package se.storkforge.petconnect.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
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
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
        Principal mockPrincipal = () -> "testUser";

        mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO))
                        .principal(mockPrincipal))
                .andExpect(status().isCreated());

        verify(reminderService).createReminder(any(ReminderInputDTO.class), eq("testUser")); // Verify the service method was called
    }

    @Test
    void getUpcomingReminders_ShouldReturnOkStatusAndList() throws Exception {
        ZoneId swedishTimeZone = ZoneId.of("Europe/Stockholm");
        LocalDateTime now = LocalDateTime.now(swedishTimeZone);
        LocalDateTime future = now.plusDays(7);
        ReminderResponseDTO responseDTO = new ReminderResponseDTO();
        responseDTO.setTitle("Upcoming Reminder");
        List<ReminderResponseDTO> reminders = Collections.singletonList(responseDTO);

        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        Mockito.when(reminderService.getUpcomingReminders(
                usernameCaptor.capture(),
                fromCaptor.capture(),
                toCaptor.capture()
        )).thenReturn(reminders);

        mockMvc.perform(get("/api/reminders/upcoming")
                        .principal(() -> "testUser")
                        .accept(MediaType.APPLICATION_JSON)) // Explicitly accept JSON
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$[0].title").value("Upcoming Reminder"));

        // Assert that the captured arguments are within the expected range
        assertEquals("testUser", usernameCaptor.getValue());
        assertFalse(fromCaptor.getValue().isBefore(now));
        assertFalse(fromCaptor.getValue().isAfter(future));
        assertFalse(toCaptor.getValue().isBefore(now));
        assertFalse(toCaptor.getValue().isAfter(future));
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

        // Simulate an authenticated user
        Principal mockPrincipal = () -> "testUser";

        mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO))
                        .principal(mockPrincipal)) // Add the mock Principal here!
                .andExpect(status().isBadRequest());
    }
}

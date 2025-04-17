package se.storkforge.petconnect.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import se.storkforge.petconnect.dto.ReminderInputDTO;
import se.storkforge.petconnect.dto.ReminderResponseDTO;
import se.storkforge.petconnect.service.ReminderService;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    private final ZoneId swedishTimeZone = ZoneId.of("Europe/Stockholm");
    private LocalDateTime now;
    private LocalDateTime future;
    private List<ReminderResponseDTO> reminders;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reminderController).build();
        now = LocalDateTime.now(swedishTimeZone);
        future = now.plus(Duration.ofDays(7).plusSeconds(5));
        ReminderResponseDTO responseDTO = new ReminderResponseDTO();
        responseDTO.setTitle("Upcoming Reminder");
        reminders = Collections.singletonList(responseDTO);
    }

    @Test
    void createReminder_ShouldReturnCreatedStatus() throws Exception {
        ReminderInputDTO inputDTO = new ReminderInputDTO();
        inputDTO.setPetId(1L);
        inputDTO.setTitle("Vet Appointment");
        inputDTO.setType("Medical");
        inputDTO.setScheduledDate(LocalDateTime.now().plusDays(1));

        ReminderResponseDTO createdResponse = new ReminderResponseDTO();
        createdResponse.setId(123L);
        createdResponse.setPetId(inputDTO.getPetId()); // Set the petId in the mock response
        createdResponse.setTitle(inputDTO.getTitle());
        createdResponse.setType(inputDTO.getType());
        createdResponse.setScheduledDate(inputDTO.getScheduledDate());

        // Simulate the behavior of the reminderService.createReminder
        when(reminderService.createReminder(any(ReminderInputDTO.class), eq("testUser")))
                .thenReturn(createdResponse);

        // Simulate an authenticated user
        Principal mockPrincipal = () -> "testUser";

        mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO))
                        .principal(mockPrincipal))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(123L)); // Verify the ID in the response

        verify(reminderService).createReminder(any(ReminderInputDTO.class), eq("testUser")); // Verify the service method was called
    }

    @Test
    void getUpcomingReminders_ShouldReturnOkStatusAndList() throws Exception {
        // Arrange (set up specific mock behavior for this test)
        when(reminderService.getUpcomingReminders(
                anyString(),
                Mockito.any(LocalDateTime.class),
                Mockito.any(LocalDateTime.class)
        )).thenReturn(reminders);

        mockMvc.perform(get("/api/reminders/upcoming")
                        .principal(() -> "testUser")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").value("Upcoming Reminder"));

        // Verifiera att metoden anropades (argumentfångarna verifieras inte här längre)
        verify(reminderService).getUpcomingReminders(anyString(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void deleteReminder_ShouldReturnNoContentStatus() throws Exception {
        Long reminderId = 1L;

        mockMvc.perform(delete("/api/reminders/{id}", reminderId)
                        .principal(() -> "testUser"))
                .andExpect(status().isNoContent());

        verify(reminderService).deleteReminder(eq(reminderId), eq("testUser"));
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
package se.storkforge.petconnect.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import se.storkforge.petconnect.config.TestWebMvcConfig;
import se.storkforge.petconnect.dto.ReminderInputDTO;
import se.storkforge.petconnect.dto.ReminderResponseDTO;
import se.storkforge.petconnect.service.ReminderService;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@Import(TestWebMvcConfig.class) // Import our test configuration

@ExtendWith(MockitoExtension.class)
class ReminderControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

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
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()); // Ensure our ObjectMapper is correctly configured
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
        createdResponse.setPetId(inputDTO.getPetId());
        createdResponse.setTitle(inputDTO.getTitle());
        createdResponse.setType(inputDTO.getType());
        createdResponse.setScheduledDate(inputDTO.getScheduledDate());

        // Simulate the behavior of the reminderService.createReminder
        when(reminderService.createReminder(any(ReminderInputDTO.class), eq("testUser")))
                .thenReturn(createdResponse);

        // Simulate an authenticated user
        Principal mockPrincipal = () -> "testUser";

        MvcResult result = mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO))
                        .principal(mockPrincipal))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(123L)) // Verify the ID in the response directly
                .andReturn(); // Get the MvcResult if you need to inspect further

        String responseContent = result.getResponse().getContentAsString();
        System.out.println("Response Content: " + responseContent); // You can still print the content

        verify(reminderService).createReminder(any(ReminderInputDTO.class), eq("testUser")); // Verify the service method was called once
    }

    @Test
    void getUpcomingReminders_ShouldReturnOkStatusAndList() throws Exception {
        // Arrange (set up specific mock behavior for this test)
        when(reminderService.getUpcomingReminders(eq("testUser"))) // Ändrat mock-anropet
                .thenReturn(reminders);

        mockMvc.perform(get("/api/reminders/upcoming")
                        .principal(() -> "testUser")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").value("Upcoming Reminder"));

        // Verifiera att metoden anropades med endast användarnamnet
        verify(reminderService).getUpcomingReminders(eq("testUser"));
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

    @Test
    void createReminder_WithDifferentValidData_ShouldReturnCreatedStatusAndCorrectDetails() throws Exception {
        ReminderInputDTO inputDTO = new ReminderInputDTO();
        inputDTO.setPetId(2L);
        inputDTO.setTitle("Walk in the park");
        inputDTO.setType("Activity");
        LocalDateTime scheduledDateTime = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
        inputDTO.setScheduledDate(scheduledDateTime);
        inputDTO.setNotes("Remember to bring a leash!");

        ReminderResponseDTO createdResponse = new ReminderResponseDTO();
        createdResponse.setId(456L);
        createdResponse.setPetId(inputDTO.getPetId());
        createdResponse.setTitle(inputDTO.getTitle());
        createdResponse.setType(inputDTO.getType());
        createdResponse.setScheduledDate(scheduledDateTime);
        createdResponse.setNotes(inputDTO.getNotes());

        when(reminderService.createReminder(any(ReminderInputDTO.class), eq("testUser")))
                .thenReturn(createdResponse);

        Principal mockPrincipal = () -> "testUser";

        MvcResult result = mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO))
                        .principal(mockPrincipal))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        ReminderResponseDTO actualResponse = objectMapper.readValue(responseContent, ReminderResponseDTO.class);

        assertEquals(456L, actualResponse.getId());
        assertEquals(2L, actualResponse.getPetId());
        assertEquals("Walk in the park", actualResponse.getTitle());
        assertEquals("Activity", actualResponse.getType());
        assertEquals(scheduledDateTime, actualResponse.getScheduledDate()); // Compare LocalDateTime objects directly
        assertEquals("Remember to bring a leash!", actualResponse.getNotes());

        verify(reminderService).createReminder(any(ReminderInputDTO.class), eq("testUser"));
    }
    @Test
    void createReminder_WithNullNotes_ShouldReturnCreatedStatusAndNullNotes() throws Exception {
        ReminderInputDTO inputDTO = new ReminderInputDTO();
        inputDTO.setPetId(1L);
        inputDTO.setTitle("Feeding time");
        inputDTO.setType("Care");
        inputDTO.setScheduledDate(LocalDateTime.now().plusHours(1));
        inputDTO.setNotes(null);

        ReminderResponseDTO createdResponse = new ReminderResponseDTO();
        createdResponse.setId(789L);
        createdResponse.setPetId(inputDTO.getPetId());
        createdResponse.setTitle(inputDTO.getTitle());
        createdResponse.setType(inputDTO.getType());
        createdResponse.setScheduledDate(inputDTO.getScheduledDate());
        createdResponse.setNotes(null);

        when(reminderService.createReminder(any(ReminderInputDTO.class), eq("testUser")))
                .thenReturn(createdResponse);

        Principal mockPrincipal = () -> "testUser";

        mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO))
                        .principal(mockPrincipal))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(789L))
                .andExpect(jsonPath("$.notes").doesNotExist()); // Or .isNull() depending on Jackson's handling

        verify(reminderService).createReminder(any(ReminderInputDTO.class), eq("testUser"));
    }
    @Test
    void getUpcomingReminders_ShouldReturnOkStatusAndListOfReminders() throws Exception {
        // Arrange
        Principal mockPrincipal = () -> "testUser";

        when(reminderService.getUpcomingReminders(eq("testUser"))) // Korrigerad rad
                .thenReturn(reminders);

        // Act & Assert
        mockMvc.perform(get("/api/reminders/upcoming")
                        .principal(mockPrincipal)
                        .accept(MediaType.APPLICATION_JSON)) // Explicitly set Accept header
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").value("Upcoming Reminder"));

        verify(reminderService).getUpcomingReminders(eq("testUser")); // Korrigerad rad
    }

    @Test
    void getUpcomingReminders_ShouldReturnNoContentStatusWhenNoReminders() throws Exception {
        // Arrange
        Principal mockPrincipal = () -> "testUser";

        when(reminderService.getUpcomingReminders(eq("testUser"))) // Ändrat mock-anropet
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/reminders/upcoming")
                        .principal(mockPrincipal)
                        .accept(MediaType.APPLICATION_JSON)) // Explicitly set Accept header
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // Verifiera att metoden anropades med endast användarnamnet
        verify(reminderService).getUpcomingReminders(eq("testUser"));
    }
    @Test
    void deleteReminder_ShouldReturnNoContentOnSuccessfulDeletion() throws Exception {
        // Arrange
        Principal mockPrincipal = () -> "testUser";
        Long reminderIdToDelete = 1L;

        // Do not need to mock the return value of deleteReminder as it's void.
        // We are primarily interested in verifying that it's called.
        doNothing().when(reminderService).deleteReminder(eq(reminderIdToDelete), eq("testUser"));

        // Act & Assert
        mockMvc.perform(delete("/api/reminders/{id}", reminderIdToDelete)
                        .principal(mockPrincipal))
                .andExpect(status().isNoContent());

        verify(reminderService).deleteReminder(eq(reminderIdToDelete), eq("testUser"));
    }}




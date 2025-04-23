package se.storkforge.petconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.builder.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.storkforge.petconnect.dto.MeetUpRequestDTO;
import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.service.MeetUpService;

import java.time.LocalDateTime;
import java.util.*;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MeetUpControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private MeetUpService meetUpService;

    @InjectMocks
    private MeetUpController meetUpController;

    private MeetUp testMeetUp;
    private final double testLongitude = 11.97;
    private final double testLatitude = 57.70;

    @BeforeEach
    void setUp() {
        // Configure ObjectMapper for Java 8 date/time handling
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Set up MockMvc with custom ObjectMapper
        mockMvc = MockMvcBuilders.standaloneSetup(meetUpController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        // Create test MeetUp with location
        Point<G2D> location = DSL.point(WGS84, DSL.g(testLongitude, testLatitude));

        testMeetUp = new MeetUp();
        testMeetUp.setId(1L);
        testMeetUp.setLocation(location);
        testMeetUp.setDateTime(LocalDateTime.now().plusDays(1));
        testMeetUp.setParticipants(new HashSet<>());
        testMeetUp.setStatus("PLANNED");
    }

    @Test
    void testCreateMeetUp_shouldReturnCreatedMeetUp() throws Exception {
        // Given
        MeetUpRequestDTO requestDTO = new MeetUpRequestDTO();
        requestDTO.setLatitude(testLatitude);
        requestDTO.setLongitude(testLongitude);
        requestDTO.setDateTime(LocalDateTime.now().plusDays(1));
        requestDTO.setParticipantIds(Collections.singletonList(1L));
        requestDTO.setStatus("PLANNED");

        when(meetUpService.planMeetUp(
                        eq(testLatitude),
                        eq(testLongitude),
                        any(LocalDateTime.class),
                        eq(Collections.singletonList(1L))))
                .thenReturn(testMeetUp);

        // When & Then
        mockMvc.perform(post("/meetups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andExpect(jsonPath("$.longitude").value(testLongitude))  // Uppdaterad assertion
                .andExpect(jsonPath("$.latitude").value(testLatitude));   // Uppdaterad assertion

        verify(meetUpService).planMeetUp(
                eq(testLatitude),
                eq(testLongitude),
                any(LocalDateTime.class),
                eq(Collections.singletonList(1L)));
    }

    @Test
    void testSearchMeetUpsEndPoint() throws Exception {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        List<MeetUp> expectedMeetups = Collections.singletonList(testMeetUp);

        when(meetUpService.searchMeetUps(
                eq(testLongitude),
                eq(testLatitude),
                eq(10.0),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class),
                any(LocalDateTime.class))
        ).thenReturn(expectedMeetups);

        // When & Then
        mockMvc.perform(get("/meetups/search")
                        .param("longitude", String.valueOf(testLongitude))
                        .param("latitude", String.valueOf(testLatitude))
                        .param("radiusInKm", "10.0")
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].longitude").value(testLongitude))
                .andExpect(jsonPath("$[0].latitude").value(testLatitude))
                .andExpect(jsonPath("$[0].status").value("PLANNED"));

        verify(meetUpService).searchMeetUps(
                eq(testLongitude),
                eq(testLatitude),
                eq(10.0),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
    }

    @Test
    void testAddParticipant_shouldReturnUpdatedMeetUp() throws Exception {
        // Given
        String userJson = """
        {
            "id": 2,
            "email": "new@example.com"
        }
        """;

        User newParticipant = new User();
        newParticipant.setId(2L);
        newParticipant.setEmail("new@example.com");

        MeetUp mockMeetUp = new MeetUp();
        mockMeetUp.setId(1L);
        mockMeetUp.setParticipants(new HashSet<>(Set.of(newParticipant)));
        mockMeetUp.setStatus("PLANNED");
        mockMeetUp.setLocation(DSL.point(WGS84, DSL.g(11.97, 57.70)));
        mockMeetUp.setDateTime(LocalDateTime.now().plusDays(1));

        when(meetUpService.addParticipant(eq(1L), any(User.class)))
                .thenReturn(mockMeetUp);

        // When & Then
        mockMvc.perform(post("/meetups/1/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.participants[0].id").value(2))
                .andExpect(jsonPath("$.participants[0].email").value("new@example.com"))
                .andExpect(jsonPath("$.status").value("PLANNED"));

        verify(meetUpService).addParticipant(eq(1L), argThat(user ->
                user.getId().equals(2L) &&
                        "new@example.com".equals(user.getEmail())
        ));
    }
    @Test
    void testRemoveParticipant_shouldReturnUpdatedMeetUp() throws Exception {
        // Given
        User participant = new User();
        participant.setId(2L);
        testMeetUp.getParticipants().add(participant);

        when(meetUpService.removeParticipant(1L, 2L))
                .thenReturn(testMeetUp);

        // When & Then
        mockMvc.perform(delete("/meetups/1/participants/2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participants").isArray())
                .andExpect(jsonPath("$.participants.length()").value(1));

        verify(meetUpService).removeParticipant(1L, 2L);
    }

    @Test
    void testGetParticipants_shouldReturnListOfUsers() throws Exception {
        // Given
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");  // Använd email istället för username

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");

        Set<User> participants = new HashSet<>(Arrays.asList(user1, user2));
        when(meetUpService.getParticipants(1L)).thenReturn(participants);

        // When & Then
        mockMvc.perform(get("/meetups/1/participants")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].email", containsInAnyOrder("user1@example.com", "user2@example.com")));

        verify(meetUpService).getParticipants(1L);
    }

    @Test
    void testRemoveParticipant_nonExistingUser_shouldReturnNotFound() throws Exception {
        // Given
        when(meetUpService.removeParticipant(1L, 99L))
                .thenThrow(new NoSuchElementException("User not found in participants"));

        // When & Then
        mockMvc.perform(delete("/meetups/1/participants/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())  // Ändrat från isBadRequest() till isNotFound()
                .andExpect(content().string("\"User not found in participants\""));
    }
}
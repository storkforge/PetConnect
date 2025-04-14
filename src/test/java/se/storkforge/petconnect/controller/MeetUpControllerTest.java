package se.storkforge.petconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.builder.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.service.MeetUpService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MeetUpControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MeetUpService meetUpService;

    @InjectMocks
    private MeetUpController meetUpController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MeetUp testMeetUp;
    private final double longitude = 11.97;
    private final double latitude = 57.70;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(meetUpController).build();

        Point<G2D> point = DSL.point(WGS84, new G2D(longitude, latitude));

        testMeetUp = new MeetUp();
        testMeetUp.setLocation(point);
        testMeetUp.setDateTime(LocalDateTime.now().plusDays(1));
        testMeetUp.setParticipants(new HashSet<>());
        testMeetUp.setStatus("PLANNED");

    }

    @Test
    public void testSearchMeetUpsEndPoint() throws Exception {

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        List<MeetUp> meetUps = List.of(testMeetUp);
        when(meetUpService.searchMeetUps(eq(longitude), eq(latitude), eq(10.0), any(), any()))
                .thenReturn(meetUps);

        mockMvc.perform(
                        get("/meetups/search")
                                .param("longitude", String.valueOf(longitude))
                                .param("latitude", String.valueOf(latitude))
                                .param("radiusInKm", "10.0")
                                .param("start", start.toString())
                                .param("end", end.toString())
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].longitude").value(longitude))
                .andExpect(jsonPath("$[0].latitude").value(latitude))
                .andExpect(jsonPath("$[0].status").value("PLANNED"));

    }


    @Test
    void testAddParticipant_shouldReturnUpdatedMeetUp() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        testMeetUp.getParticipants().add(user);

        when(meetUpService.addParticipant(eq(1L), any(User.class))).thenReturn(testMeetUp);

        mockMvc.perform(post("/meetups/1/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantIds.length()").value(1))
                .andExpect(jsonPath("$.participantIds[0]").value(1));
    }

    @Test
    void testRemoveParticipant_shouldReturnUpdatedMeetUp() throws Exception {
        User user = new User();
        user.setId(1L);
        testMeetUp.getParticipants().add(user);

        when(meetUpService.removeParticipant(1L, 1L)).thenReturn(testMeetUp);

        mockMvc.perform(delete("/meetups/1/participants/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.participantIds.length()").value(1))
                .andExpect(jsonPath("$.participantIds[0]").value(1));
    }

    @Test
    void testGetParticipants_shouldReturnListOfUsers() throws Exception {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("john");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("jane");

        Set<User> participants = new HashSet<>(List.of(user1, user2));

        when(meetUpService.getParticipants(1L)).thenReturn(participants);

        mockMvc.perform(get("/meetups/1/participants")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].username", containsInAnyOrder("john", "jane")));
    }
}

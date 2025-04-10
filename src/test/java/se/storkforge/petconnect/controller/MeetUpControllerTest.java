package se.storkforge.petconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import se.storkforge.petconnect.service.MeetUpService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(meetUpController).build();
        testMeetUp = new MeetUp();
        testMeetUp.setLocation("Test Location");
        testMeetUp.setDateTime(LocalDateTime.now().plusDays(1));
        testMeetUp.setParticipants(new HashSet<>());
        testMeetUp.setStatus("PLANNED");

    }

    @Test
    public void testSearchMeetUpsEndPoint() throws Exception {
        List<MeetUp> meetUps = Arrays.asList(testMeetUp);
        when(meetUpService.searchMeetUps(eq("Test Location"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(meetUps);
        mockMvc.perform(
                        get("/meetups/search")
                                .param("location", "Test Location")
                                .param("start", LocalDateTime.now().minusDays(1).toString())
                                .param("end", LocalDateTime.now().plusDays(2).toString())
                                .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].location").value("Test Location"));

        verify(meetUpService, times(1))
                .searchMeetUps(eq("Test Location"), any(LocalDateTime.class), any(LocalDateTime.class));

    }
}

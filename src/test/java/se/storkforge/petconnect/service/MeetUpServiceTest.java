package se.storkforge.petconnect.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.repository.MeetUpRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetUpServiceTest {

    @Mock
    private MeetUpRepository meetUpRepository;

    @InjectMocks
    private MeetUpService meetUpService;

    private MeetUp createMeetUp(String location, LocalDateTime dateTime, List<User> participants) {
        MeetUp meetUp = new MeetUp();
        meetUp.setLocation(location);
        meetUp.setDateTime(dateTime);
        meetUp.setParticipants(participants);
        meetUp.setStatus("PLANNED");
        return meetUp;
    }

    @Test
    void searchMeetUps_validCriteria_shouldReturnMeetUps() {
        // Arrange
        LocalDateTime inRangeDate = LocalDateTime.now().plusDays(1);
        LocalDateTime outOfRangeDate = LocalDateTime.now().plusDays(5);
        MeetUp inRangeMeetUp = createMeetUp("location", inRangeDate, new ArrayList<>());
        MeetUp outOfRangeMeetUp = createMeetUp("location", outOfRangeDate, new ArrayList<>());
        List<MeetUp> allMeetUps = Arrays.asList(inRangeMeetUp, outOfRangeMeetUp);
        when(meetUpRepository.findByLocationContaining(anyString())).thenReturn(allMeetUps);

        // Act
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(2);
        List<MeetUp> result = meetUpService.searchMeetUps("location", startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(inRangeDate, result.get(0).getDateTime());
    }

    @Test
    void isUserAvailable_userAvailable_shouldReturnTrue() {
        // Arrange
        User user = new User();
        user.setMeetUps(new HashSet<>());

        // Act
        boolean result = meetUpService.isUserAvailable(user, LocalDateTime.now());

        // Assert
        assertTrue(result);
    }

    @Test
    void isUserAvailable_userNotAvailable_shouldReturnFalse() {
        User user = new User();
        HashSet<MeetUp> existingMeetUps = new HashSet<>();
        LocalDateTime meetUpTime = LocalDateTime.now();
        MeetUp existingMeetUp = createMeetUp("Test location", meetUpTime, new ArrayList<>());
        existingMeetUps.add(existingMeetUp);
        user.setMeetUps(existingMeetUps);

        boolean result = meetUpService.isUserAvailable(user, meetUpTime);

        assertFalse(result);
    }

    @Test
    void planMeetUp_validMeetUp_shouldSaveMeetUp() {
        // Arrange
        List<User> participants = Arrays.asList(new User());
        MeetUp meetUp = createMeetUp("location", LocalDateTime.now().plusDays(1), participants);
        when(meetUpRepository.save(any(MeetUp.class))).thenReturn(meetUp);

        // Act
        MeetUp result = meetUpService.planMeetUp("location", LocalDateTime.now().plusDays(1), participants);

        // Assert
        assertNotNull(result);
        assertEquals("PLANNED", result.getStatus());
    }
}

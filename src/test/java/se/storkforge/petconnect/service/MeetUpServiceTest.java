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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
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
        meetUp.setParticipants(new HashSet<>(participants));
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
        assertFalse(result.isEmpty());
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

    @Test
    void addParticipant_userAvailable_shouldAddUser() {
        MeetUp meetUp = createMeetUp("location", LocalDateTime.now().plusDays(1), new ArrayList<>());
        User user = new User();
        user.setId(1L);
        user.setMeetUps(new HashSet<>()); // tomt schema = tillgänglig

        when(meetUpRepository.findById(anyLong())).thenReturn(Optional.of(meetUp));
        when(meetUpRepository.save(any(MeetUp.class))).thenReturn(meetUp);

        MeetUp updated = meetUpService.addParticipant(1L, user);

        assertTrue(updated.getParticipants().contains(user));
        verify(meetUpRepository).save(meetUp);
    }

    @Test
    void removeParticipant_existingUser_shouldRemoveUser() {
        User user = new User();
        user.setId(1L);

        MeetUp meetUp = createMeetUp("location", LocalDateTime.now().plusDays(1), List.of(user));

        when(meetUpRepository.findById(anyLong())).thenReturn(Optional.of(meetUp));
        when(meetUpRepository.save(any(MeetUp.class))).thenReturn(meetUp);

        MeetUp result = meetUpService.removeParticipant(1L, 1L);

        assertFalse(result.getParticipants().contains(user));
        verify(meetUpRepository).save(meetUp);
    }

    @Test
    void getParticipants_validMeetUpId_shouldReturnParticipants() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        Set<User> participants = new HashSet<>(List.of(user1, user2));
        MeetUp meetUp = createMeetUp("loc", LocalDateTime.now().plusDays(1), new ArrayList<>());
        meetUp.setParticipants(participants);

        when(meetUpRepository.findById(anyLong())).thenReturn(Optional.of(meetUp));

        Set<User> result = meetUpService.getParticipants(1L);

        assertEquals(2, result.size());
        assertTrue(result.contains(user1));
    }
}

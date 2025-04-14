package se.storkforge.petconnect.service;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.builder.DSL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.repository.MeetUpRepository;
import se.storkforge.petconnect.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetUpServiceTest {

    @Mock
    private MeetUpRepository meetUpRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MeetUpService meetUpService;

    private MeetUp createMeetUp(Point<G2D> location, LocalDateTime dateTime, List<User> participants) {
        double latitude = 0;
        double longitude = 0;
        location = DSL.point(WGS84, new G2D(longitude, latitude));

        MeetUp meetUp = new MeetUp();
        meetUp.setLocation(location);
        meetUp.setDateTime(dateTime);
        meetUp.setParticipants(new HashSet<>(participants));
        meetUp.setStatus("PLANNED");
        return meetUp;
    }

    @Test
    void searchMeetUps_validCriteria_shouldReturnMeetUps() {

        double testLongitude = 11.97;
        double testLatitude = 57.70;
        double radiusInKm = 10.0;

        LocalDateTime inRangeDate = LocalDateTime.now().plusDays(1);
        LocalDateTime outOfRangeDate = LocalDateTime.now().plusDays(5);

        Point<G2D> centerPoint = DSL.point(WGS84, new G2D(testLongitude, testLatitude));

        MeetUp inRangeMeetUp = createMeetUp(centerPoint, inRangeDate, new ArrayList<>());
        MeetUp outOfRangeMeetUp = createMeetUp(centerPoint, outOfRangeDate, new ArrayList<>());
        List<MeetUp> allMeetUps = Arrays.asList(inRangeMeetUp, outOfRangeMeetUp);

        when(meetUpRepository.findAll()).thenReturn(allMeetUps);


        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(2);
        List<MeetUp> result = meetUpService.searchMeetUps(testLongitude, testLatitude, radiusInKm, startDate, endDate);


        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains(inRangeMeetUp));
        assertFalse(result.contains(outOfRangeMeetUp));
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

        Point<G2D> point = DSL.point(WGS84, new G2D(11.97, 57.70));

        MeetUp existingMeetUp = createMeetUp(point, meetUpTime, new ArrayList<>());
        existingMeetUps.add(existingMeetUp);
        user.setMeetUps(existingMeetUps);

        boolean result = meetUpService.isUserAvailable(user, meetUpTime);

        assertFalse(result);
    }

    @Test
    void planMeetUp_validMeetUp_shouldSaveMeetUp() {
        // Arrange
        double latitude = 57.70;
        double longitude = 11.97;
        LocalDateTime dateTime = LocalDateTime.now().plusDays(1);

        User user = new User();
        user.setId(1L);
        List<Long> participantIds = List.of(1L);
        List<User> participants = List.of(user);

        MeetUp meetUp = new MeetUp();
        meetUp.setLocation(DSL.point(WGS84, new G2D(longitude, latitude)));
        meetUp.setDateTime(dateTime);
        meetUp.setParticipants(new HashSet<>(participants));
        meetUp.setStatus("PLANNED");

        when(userRepository.findAllById(participantIds)).thenReturn(participants);
        when(meetUpRepository.save(any(MeetUp.class))).thenReturn(meetUp);

        // Act
        MeetUp result = meetUpService.planMeetUp(latitude, longitude, dateTime, participantIds);

        // Assert
        assertNotNull(result);
        assertEquals("PLANNED", result.getStatus());
        assertEquals(dateTime, result.getDateTime());
        assertEquals(1, result.getParticipants().size());
    }

    @Test
    void addParticipant_userAvailable_shouldAddUser() {

        User user = new User();
        user.setId(1L);
        user.setMeetUps(new HashSet<>());

        Point<G2D> point = DSL.point(WGS84, new G2D(11.97, 57.70));

        MeetUp meetUp = createMeetUp(point, LocalDateTime.now().plusDays(1), new ArrayList<>());

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

        Point<G2D> point = DSL.point(WGS84, new G2D(11.97, 57.70));

        MeetUp meetUp = createMeetUp(point, LocalDateTime.now().plusDays(1), List.of(user));

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
        Point<G2D> point = DSL.point(WGS84, new G2D(11.97, 57.70));
        Set<User> participants = new HashSet<>(List.of(user1, user2));
        MeetUp meetUp = createMeetUp(point, LocalDateTime.now().plusDays(1), new ArrayList<>());
        meetUp.setParticipants(participants);

        when(meetUpRepository.findById(anyLong())).thenReturn(Optional.of(meetUp));

        Set<User> result = meetUpService.getParticipants(1L);

        assertEquals(2, result.size());
        assertTrue(result.contains(user1));
    }
}

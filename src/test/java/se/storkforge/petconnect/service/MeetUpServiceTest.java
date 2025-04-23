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
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private MeetUpService meetUpService;

    private MeetUp createMeetUp(Point<G2D> location, LocalDateTime dateTime, List<User> participants) {
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

        LocalDateTime now = LocalDateTime.of(2025, 4, 14, 12, 0);
        LocalDateTime inRangeDate = now.plusDays(1);
        LocalDateTime outOfRangeDate = now.plusDays(5);

        LocalDateTime endDate = now.plusDays(2);

        Point<G2D> location = DSL.point(WGS84, new G2D(testLongitude, testLatitude));

        MeetUp inRangeMeetUp = createMeetUp(location, inRangeDate, List.of());

        // ✅ Updated to match the new repository method
        when(meetUpRepository.findMeetUpsNearAndWithinTime(
                eq(testLongitude), eq(testLatitude), eq(radiusInKm * 1000), eq(now), eq(endDate)
        )).thenReturn(List.of(inRangeMeetUp));

        List<MeetUp> result = meetUpService.searchMeetUps(
                testLongitude, testLatitude, radiusInKm, now, endDate
        );

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(m -> m.getDateTime().equals(inRangeDate)));
        assertTrue(result.stream().noneMatch(m -> m.getDateTime().equals(outOfRangeDate)));
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
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setMeetUps(new HashSet<>());

        Point<G2D> point = DSL.point(WGS84, new G2D(11.97, 57.70));
        MeetUp meetUp = createMeetUp(point, LocalDateTime.now().plusDays(1), new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(meetUpRepository.findById(anyLong())).thenReturn(Optional.of(meetUp));
        when(meetUpRepository.save(any(MeetUp.class))).thenReturn(meetUp);

        // Act
        MeetUp updated = meetUpService.addParticipant(1L, user);

        // Assert
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

    @Test
    void notifyParticipants_validMeetUp_shouldSendEmailsAndSmsToAll() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPhoneNumber("+46762373333");

        MeetUp meetUp = new MeetUp();
        meetUp.setParticipants(Set.of(user));
        meetUp.setDateTime(LocalDateTime.now().plusDays(1));
        meetUp.setLocation(DSL.point(WGS84, new G2D(11.97, 57.70)));
        meetUp.setStatus("PLANNED");

        // Act
        meetUpService.notifyAllParticipants(meetUp);

        // Assert: just verify notificationService was called with the correct user
        verify(notificationService).notifyUser(eq(user), anyString(), anyString());
    }

    @Test
    void searchMeetUps_noMatchingMeetUps_shouldReturnEmptyList() {
        double longitude = 11.97;
        double latitude = 57.70;
        double radiusInKm = 10.0;
        LocalDateTime start = LocalDateTime.of(2025, 4, 14, 12, 0);
        LocalDateTime end = start.plusDays(1);

        when(meetUpRepository.findMeetUpsNearAndWithinTime(
                anyDouble(), anyDouble(), anyDouble(), any(), any())
        ).thenReturn(Collections.emptyList());

        List<MeetUp> result = meetUpService.searchMeetUps(longitude, latitude, radiusInKm, start, end);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Expected no meetups within range");
    }

    @Test
    void searchMeetUps_invalidDateRange_shouldThrowException() {
        double longitude = 11.97;
        double latitude = 57.70;
        double radiusInKm = 10.0;

        LocalDateTime start = LocalDateTime.of(2025, 4, 16, 12, 0);
        LocalDateTime end = LocalDateTime.of(2025, 4, 14, 12, 0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                meetUpService.searchMeetUps(longitude, latitude, radiusInKm, start, end)
        );

        assertEquals("Start date must be before end date", exception.getMessage());
    }

}

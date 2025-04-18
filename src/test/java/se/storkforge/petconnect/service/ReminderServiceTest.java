package se.storkforge.petconnect.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.entity.ReminderPreferences;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.repository.MeetUpRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReminderServiceTest {

    @Mock
    private MeetUpRepository meetUpRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReminderService reminderService;

    @BeforeEach
    void setUp() {
        try {MockitoAnnotations.openMocks(this);} catch (Exception e) {throw new RuntimeException(e);}
    }

    @Test
    void checkUpcomingMeetups_ShouldSendReminder() {
        LocalDateTime meetUpTime = LocalDateTime.now().plusHours(2);

        User user = new User();
        user.setReminderPreferences(new ReminderPreferences());

        MeetUp meetUp = new MeetUp();
        meetUp.setDateTime(meetUpTime);

        Map<User, Boolean> reminders = new HashMap<>();
        reminders.put(user, true);
        meetUp.setReminders(reminders);

        when(meetUpRepository.findAll()).thenReturn(List.of(meetUp));

        reminderService.checkUpcomingMeetups();

        verify(meetUpRepository, times(1)).save(meetUp);
        assertFalse(meetUp.getReminders().get(user));
    }

    @Test
    void checkUpcomingMeetups_ShouldNotSendReminder() {
        LocalDateTime meetUpTime = LocalDateTime.now().plusHours(30);
        User user = new User();
        user.setReminderPreferences(new ReminderPreferences());

        MeetUp meetUp = new MeetUp();
        meetUp.setDateTime(meetUpTime);

        Map<User, Boolean> reminders = new HashMap<>();
        reminders.put(user, true);
        meetUp.setReminders(reminders);

        when(meetUpRepository.findAll()).thenReturn(List.of(meetUp));

        reminderService.checkUpcomingMeetups();

        verify(notificationService, never()).notifyUser(any(), any(), any());
        verify(meetUpRepository, never()).save(any());
    }

    @Test
    void checkUpcomingMeetups_ShouldNotSendReminder_WhenAlreadySent() {
        LocalDateTime meetUpTime = LocalDateTime.now().plusHours(1);
        User user = new User();
        user.setReminderPreferences(new ReminderPreferences());

        MeetUp meetUp = new MeetUp();
        meetUp.setDateTime(meetUpTime);

        Map<User, Boolean> reminders = new HashMap<>();
        reminders.put(user, false);
        meetUp.setReminders(reminders);

        when(meetUpRepository.findAll()).thenReturn(List.of(meetUp));

        reminderService.checkUpcomingMeetups();

        verify(notificationService, never()).notifyUser(any(), any(), any());
        verify(meetUpRepository, never()).save(any());
    }
}
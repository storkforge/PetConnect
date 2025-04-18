package se.storkforge.petconnect.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.entity.ReminderPreferences;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.repository.MeetUpRepository;

import javax.security.auth.Subject;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ReminderService {
    private String REMINDER_SUBJECT = "Meetup_Reminder";

    MeetUpRepository meetUpRepository;
    NotificationService notificationService;

    public ReminderService(MeetUpRepository meetUpRepository,
                           NotificationService notificationService) {
        this.meetUpRepository = meetUpRepository;
        this.notificationService = notificationService;
    }


    @Scheduled(fixedRateString = "${properties.reminders.checkFrequency}")
    public void checkUpcomingMeetups() {
        LocalDateTime now = LocalDateTime.now(); //This might need to be changed to some other time configuration
        List<MeetUp> allMeetups = meetUpRepository.findAll();
        allMeetups.forEach(this::processMeetupReminders);
    }

    private void processMeetupReminders(MeetUp meetUp) {
        meetUp.getReminders().forEach((user, needsReminder) -> {
            if (needsReminder) {
                checkAndSendReminder(user, meetUp);
            }
        });
    }

    private void checkAndSendReminder(User user, MeetUp meetUp) {
        ReminderPreferences preferences = user.getReminderPreferences();
        LocalDateTime reminderTime = calculateReminderTime(meetUp, preferences);
        LocalDateTime now = LocalDateTime.now();

        if (shouldSendReminder(now, reminderTime)) {
            sendReminderNotification(user, meetUp);
            updateReminderStatus(meetUp, user);
        }
    }

    private LocalDateTime calculateReminderTime(MeetUp meetUp, ReminderPreferences preferences) {
        return meetUp.getDateTime().minusHours(preferences.getRemindHoursBefore());
    }

    private boolean shouldSendReminder(LocalDateTime now, LocalDateTime reminderTime) {
        return now.isAfter(reminderTime) || now.isEqual(reminderTime);
    }

    private void sendReminderNotification(User user, MeetUp meetUp) {
        String message = String.format("Don't forget your meet up at %s at %s",
                meetUp.getDateTime(), meetUp.getLocation());
        notificationService.notifyUser(user, REMINDER_SUBJECT, message);
    }

    private void updateReminderStatus(MeetUp meetUp, User user) {
        meetUp.getReminders().put(user, false);
        meetUpRepository.save(meetUp);
    }

}

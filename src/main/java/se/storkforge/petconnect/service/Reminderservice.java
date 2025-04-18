package se.storkforge.petconnect.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.entity.ReminderPreferences;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.repository.MeetUpRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class ReminderService {
    MeetUpRepository meetUpRepository;
    NotificationService notificationService;


    @Scheduled(fixedRateString = "${properties.reminders.checkFrequency}")
    public void checkUpcomingMeetups() {
        LocalDateTime now = LocalDateTime.now(); //This might need to be changed to some other time configuration
        List<MeetUp> upcoming = meetUpRepository.findAll();
        upcoming.forEach(meetUp -> {
            Set<User> meetUpParticipants =  meetUp.getParticipants();
            meetUpParticipants.forEach(participant -> {
                ReminderPreferences pref = participant.getReminderPreferences();
                LocalDateTime reminderTime = meetUp.getDateTime().minusHours(pref.getRemindHoursBefore());
                if (now.isAfter(reminderTime) || now.isEqual(reminderTime)) {
                    // Message and subject can be changed later on
                    String message = "Dont forget your meet up at, " + meetUp.getDateTime() + "at" + meetUp.getLocation();
                    notificationService.notifyUser(participant, "Meet-Up-Reminder", message );
                }
                //Issue: find a way to save that thy have been notified, otherwise it's going to keep on sending reminders

            });

        });
    }

}

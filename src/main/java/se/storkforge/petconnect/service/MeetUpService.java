package se.storkforge.petconnect.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.repository.MeetUpRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MeetUpService {
    @Autowired
    private MeetUpRepository meetUpRepository;

    public List<MeetUp> searchMeetUps(String location, LocalDateTime start, LocalDateTime end) {
        if (location == null || start == null || end == null)
            throw new IllegalArgumentException("Location, start date and end date cannot be null");
        if (start.isAfter(end)){
            throw new IllegalArgumentException("Start date most be before end date");
        }
        return meetUpRepository.findByLocationContainingAndDateTimeBetween(location, start, end);
    }

    public boolean isUserAvailable(User user, LocalDateTime dateTime) {
        if (user == null || dateTime == null)
            throw new IllegalArgumentException("User and dateTime cannot be null");

        LocalDateTime startWindow = dateTime.minusMinutes(30);
        LocalDateTime endWindow = dateTime.plusMinutes(30);
        return user.getMeetUps().stream()
                .noneMatch(meetUp -> {
                    LocalDateTime meetupTime = meetUp.getDateTime();
                    return !meetupTime.isBefore(startWindow) && !meetupTime.isAfter(endWindow);
                });
    }

    @Transactional
    public MeetUp planMeetUp(String location, LocalDateTime dateTime, List<User> participants) {
        if (location == null || location.trim().isEmpty())
            throw new IllegalArgumentException("Location cannot be null or empty");
        if (dateTime == null)
            throw new IllegalArgumentException("Date time cannot be null");
        if (participants == null || participants.isEmpty())
            throw new IllegalArgumentException("Participants cannot be null or empty");
        if (participants.stream().anyMatch(user -> !isUserAvailable(user, dateTime)))
            throw new IllegalStateException("Some users are not available at this time.");


        MeetUp meetUp = new MeetUp();
        meetUp.setLocation(location);
        meetUp.setDateTime(dateTime);
        meetUp.setParticipants(participants);
        meetUp.setStatus("PLANNED");

        return meetUpRepository.save(meetUp);
    }

}

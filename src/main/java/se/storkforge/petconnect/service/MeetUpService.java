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
        return meetUpRepository.findByLocationContaining(location)
                .stream()
                .filter(meetUp -> meetUp.getDateTime().isAfter(start) && meetUp.getDateTime().isBefore(end))
                .collect(Collectors.toList());
    }

    public boolean isUserAvailable(User user, LocalDateTime dateTime) {
        return user.getMeetUps().stream()
                .noneMatch(meetUp -> meetUp.getDateTime().equals(dateTime));
    }

    @Transactional
    public MeetUp planMeetUp(String location, LocalDateTime dateTime, List<User> participants) {
        if (participants.stream().anyMatch(user -> !isUserAvailable(user, dateTime))) {
            throw new IllegalStateException("Some users are not available at this time.");
        }

        MeetUp meetUp = new MeetUp();
        meetUp.setLocation(location);
        meetUp.setDateTime(dateTime);
        meetUp.setParticipants(participants);
        meetUp.setStatus("PLANNED");

        return meetUpRepository.save(meetUp);
    }

}

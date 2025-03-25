package se.storkforge.petconnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.storkforge.petconnect.entity.MeetUp;
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
}

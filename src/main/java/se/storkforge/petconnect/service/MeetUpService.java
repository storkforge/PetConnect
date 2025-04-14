package se.storkforge.petconnect.service;

import jakarta.transaction.Transactional;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.builder.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.repository.MeetUpRepository;
import se.storkforge.petconnect.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

@Service
public class MeetUpService {
    @Autowired
    private MeetUpRepository meetUpRepository;

    @Autowired
    UserRepository userRepository;

    /**
     * Searches for meet-ups based on location and a date-time range.
     * @param location - partial or full name of the location to filter.
     * @param start - start of the time range.
     * @param end - end of the time range.
     * @return list of meet-ups matching the criteria.
     */
    public List<MeetUp> searchMeetUps(String location, LocalDateTime start, LocalDateTime end) {
        if (location == null || start == null || end == null)
            throw new IllegalArgumentException("Location, start date and end date cannot be null");
        if (start.isAfter(end)){
            throw new IllegalArgumentException("Start date most be before end date");
        }
        return meetUpRepository.findByLocationContaining(location). stream()
                .filter(meetUp -> !meetUp.getDateTime().isBefore(start) && !meetUp.getDateTime().isAfter(end))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a user is available at a given date and time.
     * The check considers a ±30 minute window to avoid overlapping meet-ups.
     * @param user - the user to check.
     * @param dateTime - the date and time to check for availability.
     * @return true if the user is available, false otherwise.
     */
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

    /**
     * Plans and creates a new meet-up with the specified location, time, and participants.
     *
     * Validates that the date/time and participant list are provided,
     * that all participant IDs correspond to existing users,
     * and that all users are available at the specified time.
     *
     * Converts the given latitude and longitude into a geographic Point using the WGS84 coordinate system.
     *
     * @param latitude the latitude of the meet-up location
     * @param longitude the longitude of the meet-up location
     * @param dateTime the scheduled date and time for the meet-up
     * @param participantIds list of user IDs to be added as participants
     * @return the persisted MeetUp entity
     * @throws IllegalArgumentException if required fields are missing
     * @throws NoSuchElementException if any participant is not found
     * @throws IllegalStateException if any user is not available at the given time
     */
    @Transactional
    public MeetUp planMeetUp(double latitude, double longitude, LocalDateTime dateTime, List<Long> participantIds) {
        if (dateTime == null)
            throw new IllegalArgumentException("Date time cannot be null");
        if (participantIds == null || participantIds.isEmpty())
            throw new IllegalArgumentException("Participants cannot be null or empty");
        List<User> participants = userRepository.findAllById(participantIds);
        if (participants.size() != participantIds.size())
            throw new NoSuchElementException("Some users were not found");
        if (participants.stream().anyMatch(user -> !isUserAvailable(user, dateTime)))
            throw new IllegalStateException("Some users are not available at this time.");

        Point<G2D> location = DSL.point(
                WGS84,
                new G2D(longitude, latitude)
        );


        MeetUp meetUp = new MeetUp();
        meetUp.setLocation(location);
        meetUp.setDateTime(dateTime);
        meetUp.setParticipants(new HashSet<>(participants));
        meetUp.setStatus("PLANNED");

        return meetUpRepository.save(meetUp);
    }

    /**
     * Adds a participant to the specified meet-up, if the user is available at the scheduled time.
     * Validates availability using the isUserAvailable method.
     *
     * @param meetUpId - the ID of the meet-up to join.
     * @param user - the user to be added as a participant.
     * @return the updated MeetUp entity with the new participant.
     * @throws NoSuchElementException if the meet-up is not found.
     * @throws IllegalStateException if the user is not available at the meet-up time.
     */
    @Transactional
    public MeetUp addParticipant(Long meetUpId, User user) {
        MeetUp meetUp = meetUpRepository.findById(meetUpId)
                .orElseThrow(() -> new NoSuchElementException("Meet-up not found"));

        if (!isUserAvailable(user, meetUp.getDateTime())) {
            throw new IllegalStateException("User is not available at the scheduled time");
        }

        meetUp.getParticipants().add(user);
        return meetUpRepository.save(meetUp);
    }

    /**
     * Removes a participant from the specified meet-up.
     *
     * @param meetUpId - the ID of the meet-up.
     * @param userId - the ID of the user to be removed.
     * @return the updated MeetUp entity without the removed participant.
     * @throws NoSuchElementException if the meet-up or the user within the participants is not found.
     */
    @Transactional
    public MeetUp removeParticipant(Long meetUpId, Long userId) {
        MeetUp meetUp = meetUpRepository.findById(meetUpId)
                .orElseThrow(() -> new NoSuchElementException("Meet-up not found"));

        boolean removed = meetUp.getParticipants().removeIf(user -> user.getId().equals(userId));
        if (!removed) {
            throw new NoSuchElementException("User not found in participants");
        }

        return meetUpRepository.save(meetUp);
    }

    /**
     * Retrieves the list of participants for the given meet-up.
     *
     * @param meetUpId - the ID of the meet-up.
     * @return a set of users participating in the meet-up.
     * @throws NoSuchElementException if the meet-up is not found.
     */
    public Set<User> getParticipants(Long meetUpId) {
        MeetUp meetUp = meetUpRepository.findById(meetUpId)
                .orElseThrow(() -> new NoSuchElementException("Meet-up not found"));

        return meetUp.getParticipants();
    }

}

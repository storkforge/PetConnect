package se.storkforge.petconnect.service;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.transaction.Transactional;
import org.geolatte.geom.C2D;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.builder.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.entity.MeetUpStatus;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.exception.UserOverbookedException;
import se.storkforge.petconnect.repository.MeetUpRepository;
import se.storkforge.petconnect.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

@Service
public class MeetUpService {

    @Autowired private MeetUpRepository meetUpRepository;
    @Autowired private MailService mailService;
    @Autowired private SmsService smsService;
    @Autowired private NotificationService notificationService;

    private static final Logger logger = LoggerFactory.getLogger(MeetUpService.class);
    @Autowired
    UserRepository userRepository;



    /**
     * Searches for meet-ups within a specified radius from a given location and time range.
     *
     * @param longitude - the longitude of the center point.
     * @param latitude - the latitude of the center point.
     * @param radiusInKm - the search radius in kilometers.
     * @param start - the start date and time for filtering meet-ups.
     * @param end - the end date and time for filtering meet-ups.
     * @return a list of meet-ups that match the criteria.
     */
    public List<MeetUp> searchMeetUps(double longitude, double latitude, double radiusInKm, LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(start, "Start date cannot be null");
        Objects.requireNonNull(end, "End date cannot be null");

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        Point<G2D> center = DSL.point(WGS84, new G2D(longitude, latitude));
        List<MeetUp> dateFilteredMeetUps = meetUpRepository.findByDateTimeBetween(start, end);

        return dateFilteredMeetUps.stream()
                .filter(m -> m.getLocation() != null)
                .filter(m -> isWithinRadius(center, m.getLocation(), radiusInKm))
                .collect(Collectors.toList());
    }

    private boolean isWithinRadius(Point<G2D> center, Point<G2D> point, double radiusKm) {
        double earthRadius = 6371.0; // km
        double lat1 = center.getPosition().getLat();
        double lat2 = point.getPosition().getLat();
        double lon1 = center.getPosition().getLon();
        double lon2 = point.getPosition().getLon();

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        return distance <= radiusKm;
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
        if (latitude < -90 || latitude > 90)
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        if (longitude < -180 || longitude > 180)
            throw new IllegalArgumentException("Longitude must be between -180 and 180");

        List<User> participants = userRepository.findAllById(participantIds);
        if (participants.size() != participantIds.size())
            throw new NoSuchElementException("Some users were not found");
        if (participants.stream().anyMatch(user -> !isUserAvailable(user, dateTime)))
            throw new UserOverbookedException("Some users are not available at this time.");

        Point<G2D> location = DSL.point(WGS84, new G2D(longitude, latitude));

        MeetUp meetUp = new MeetUp();
        meetUp.setLocation(location);
        meetUp.setDateTime(dateTime);
        meetUp.setParticipants(new HashSet<>(participants));
        meetUp.setStatus(MeetUpStatus.PLANNED.name());

        MeetUp savedMeetUp = meetUpRepository.save(meetUp);
        notifyAllParticipants(savedMeetUp);
        return savedMeetUp;
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
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User cannot be null and must have a valid ID.");
        }

        User verifiedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        MeetUp meetUp = meetUpRepository.findById(meetUpId)
                .orElseThrow(() -> new NoSuchElementException("Meet-up not found"));

        if (!isUserAvailable(verifiedUser, meetUp.getDateTime())) {
            throw new IllegalStateException("User is not available at the scheduled time.");
        }

        if (meetUp.getParticipants().contains(verifiedUser)) {
            throw new IllegalStateException("User is already a participant.");
        }

        meetUp.getParticipants().add(verifiedUser);
        MeetUp updatedMeetUp = meetUpRepository.save(meetUp);

        notifySingleParticipant(updatedMeetUp, verifiedUser);

        return updatedMeetUp;
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

    void notifyAllParticipants(MeetUp meetUp) {
        String subject = "Pet Connect: You've been invited to a meet-up!";
        String message = buildNotificationMessage(meetUp);

        List<String> failedNotifications = new ArrayList<>();

        for (User user : meetUp.getParticipants()) {
            try {
                notificationService.notifyUser(user, subject, message);
            } catch (Exception e) {
                failedNotifications.add(user.getEmail() + " / " + user.getPhoneNumber());
                logger.warn("Failed to notify participant: {} / {}", user.getEmail(), user.getPhoneNumber(), e);
            }
        }

        if (!failedNotifications.isEmpty()) {
            logger.error("{} notification(s) failed: {}", failedNotifications.size(),
                    String.join(", ", failedNotifications));
        }
    }

    private void notifySingleParticipant(MeetUp meetUp, User participant) {
        String subject = "Pet Connect: You've been invited to a meet-up!";
        String message = buildNotificationMessage(meetUp);
        notificationService.notifyUser(participant, subject, message);
    }

    private String buildNotificationMessage(MeetUp meetUp) {
        return String.format(
                "Hello! You've been invited to a pet meet-up scheduled for %s at latitude %.5f, longitude %.5f. " +
                        "Please confirm your attendance. Status: %s.",
                meetUp.getDateTime(),
                meetUp.getLocation().getPosition().getLat(),
                meetUp.getLocation().getPosition().getLon(),
                meetUp.getStatus()
        );
    }

}

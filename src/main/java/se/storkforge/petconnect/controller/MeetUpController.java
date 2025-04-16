package se.storkforge.petconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import se.storkforge.petconnect.dto.MeetUpRequestDTO;
import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.service.MeetUpService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/meetups")
public class MeetUpController {
    @Autowired
    private MeetUpService meetUpService;

    /**
     * Handles the creation of a new meet-up.
     *
     * Accepts a request body containing location coordinates (latitude and longitude),
     * date and time of the meet-up, and a list of participant user IDs.
     * Delegates to the service layer to validate and persist the meet-up.
     *
     * @param dto the data transfer object containing meet-up details
     * @return HTTP 200 with the created MeetUp object if successful,
     *         or HTTP 400 with an error message if the request is invalid
     */
    @PostMapping("/create")
    public ResponseEntity<?> createMeetUp(@RequestBody MeetUpRequestDTO dto) {
        try {
            MeetUp meetUp = meetUpService.planMeetUp(
                    dto.getLatitude(),
                    dto.getLongitude(),
                    dto.getDateTime(),
                    dto.getParticipantIds()
            );
            return ResponseEntity.ok(meetUp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public List<MeetUpRequestDTO> searchMeetUps(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam double radiusInKm,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        try {
            return meetUpService.searchMeetUps(longitude, latitude, radiusInKm, start, end)
                    .stream()
                    .map(MeetUpRequestDTO::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    /**
     * Adds a user as a participant to the specified meet-up.
     *
     * @param meetUpId - the ID of the meet-up.
     * @param user - the user to be added (provided in the request body).
     * @return HTTP 200 with the updated meet-up if successful, or HTTP 400 with an error message if failed.
     */
    @PostMapping("/{meetUpId}/participants")
    public ResponseEntity<?> addParticipant(@PathVariable Long meetUpId, @RequestBody User user) {
        try {
            MeetUp updated = meetUpService.addParticipant(meetUpId, user);
            return ResponseEntity.ok(MeetUpRequestDTO.fromEntity(updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Removes a user from the list of participants of the specified meet-up.
     *
     * @param meetUpId - the ID of the meet-up.
     * @param userId - the ID of the user to be removed.
     * @return HTTP 200 with the updated meet-up if successful, or HTTP 400 with an error message if failed.
     */
    @DeleteMapping("/{meetUpId}/participants/{userId}")
    public ResponseEntity<?> removeParticipant(
            @PathVariable Long meetUpId,
            @PathVariable Long userId) {
        try {
            MeetUp updated = meetUpService.removeParticipant(meetUpId, userId);
            return ResponseEntity.ok(MeetUpRequestDTO.fromEntity(updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Retrieves all participants of a specific meet-up.
     *
     * @param meetUpId - the ID of the meet-up.
     * @return HTTP 200 with a set of users participating in the meet-up.
     */
    @GetMapping("/{meetUpId}/participants")
    public ResponseEntity<Set<User>> getParticipants(@PathVariable Long meetUpId) {
        try {
            return ResponseEntity.ok(meetUpService.getParticipants(meetUpId));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Set.of());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", e);
        }

    }

    @GetMapping("/meetups/nearby")
    public String findNearbyMeetups(@RequestParam double lon,
                                    @RequestParam double lat,
                                    @RequestParam(defaultValue = "1000") double radius,
                                    Model model) {
        List<MeetUp> nearby = meetUpService.findNearbyMeetups(lon, lat, radius);
        model.addAttribute("meetups", nearby);
        return "meetup/placeholder"; // Replaced with our actual view name
    }


}

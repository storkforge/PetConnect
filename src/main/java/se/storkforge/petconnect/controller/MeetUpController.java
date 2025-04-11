package se.storkforge.petconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.storkforge.petconnect.entity.MeetUp;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.service.MeetUpService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/meetups")
public class MeetUpController {
    @Autowired
    private MeetUpService meetUpService;

    /**
     * Endpoint to search for meet-ups by location and date-time range.
     * @param location - part of the location name to search.
     * @param start - start of the search period.
     * @param end - end of the search period.
     * @return list of matching meet-ups.
     */
    @GetMapping("/search")
    public List<MeetUp> searchMeetUps(
            @RequestParam String location,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return meetUpService.searchMeetUps(location, start, end);
    }

    /**
     * Adds a user as a participant to the specified meet-up.
     *
     * @param meetUpId - the ID of the meet-up.
     * @param user - the user to be added (provided in the request body).
     * @return HTTP 200 with the updated meet-up if successful, or HTTP 400 with an error message if failed.
     */
    @PostMapping("/{meetUpId}/participants")
    public ResponseEntity<?> addParticipant(
            @PathVariable Long meetUpId,
            @RequestBody User user) {
        try {
            MeetUp updated = meetUpService.addParticipant(meetUpId, user);
            return ResponseEntity.ok(updated);
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
            return ResponseEntity.ok(updated);
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
        return ResponseEntity.ok(meetUpService.getParticipants(meetUpId));
    }
}

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

    @GetMapping("/search")
    public List<MeetUp> searchMeetUps(
            @RequestParam String location,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return meetUpService.searchMeetUps(location, start, end);
    }

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

    @GetMapping("/{meetUpId}/participants")
    public ResponseEntity<Set<User>> getParticipants(@PathVariable Long meetUpId) {
        return ResponseEntity.ok(meetUpService.getParticipants(meetUpId));
    }
}

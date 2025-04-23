package se.storkforge.petconnect.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import se.storkforge.petconnect.dto.MeetUpRequestDTO;
import se.storkforge.petconnect.dto.MeetUpResponseDTO;
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

    private final MeetUpService meetUpService;

    public MeetUpController(MeetUpService meetUpService) {
        this.meetUpService = meetUpService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createMeetUp(@RequestBody MeetUpRequestDTO dto) {
        try {
            MeetUp meetUp = meetUpService.planMeetUp(
                    dto.getLatitude(),
                    dto.getLongitude(),
                    dto.getDateTime(),
                    dto.getParticipantIds());
            return ResponseEntity.ok(new MeetUpResponseDTO(meetUp));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public List<MeetUpResponseDTO> searchMeetUps(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam double radiusInKm,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            return meetUpService.searchMeetUps(longitude, latitude, radiusInKm, start, end)
                    .stream()
                    .map(MeetUpResponseDTO::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{meetUpId}/participants")
    public ResponseEntity<?> addParticipant(@PathVariable Long meetUpId, @RequestBody User user) {
        try {
            MeetUp updated = meetUpService.addParticipant(meetUpId, user);
            return ResponseEntity.ok(new MeetUpResponseDTO(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{meetUpId}/participants/{userId}")
    public ResponseEntity<?> removeParticipant(
            @PathVariable Long meetUpId,
            @PathVariable Long userId) {
        try {
            MeetUp updated = meetUpService.removeParticipant(meetUpId, userId);
            return ResponseEntity.ok(new MeetUpResponseDTO(updated));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{meetUpId}/participants")
    public ResponseEntity<List<MeetUpResponseDTO.ParticipantDTO>> getParticipants(@PathVariable Long meetUpId) {
        try {
            Set<User> participants = meetUpService.getParticipants(meetUpId);
            List<MeetUpResponseDTO.ParticipantDTO> participantDTOs = participants.stream()
                    .map(user -> {
                        MeetUpResponseDTO.ParticipantDTO dto = new MeetUpResponseDTO.ParticipantDTO();
                        dto.setId(user.getId());
                        dto.setEmail(user.getEmail());
                        return dto;
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(participantDTOs);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", e);
        }
    }
}
package se.storkforge.petconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import se.storkforge.petconnect.dto.ReminderInputDTO;
import se.storkforge.petconnect.dto.ReminderResponseDTO;
import se.storkforge.petconnect.service.ReminderService;

import jakarta.validation.Valid; // Import the @Valid annotation
import java.net.URI;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    @Autowired
    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping
    public ResponseEntity<ReminderResponseDTO> createReminder(@Valid @RequestBody ReminderInputDTO reminderInputDTO, Principal principal) {
        String username = principal.getName();
        ReminderResponseDTO createdReminder = reminderService.createReminder(reminderInputDTO, username);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdReminder.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdReminder); // Now return the DTO in the body
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ReminderResponseDTO>> getUpcomingReminders(Principal principal) {
        String username = principal.getName();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(7); // Exempel: Hämta påminnelser för nästa 7 dagar
        List<ReminderResponseDTO> upcomingReminders = reminderService.getUpcomingReminders(username, now, future);
        return ResponseEntity.ok(upcomingReminders);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminder(@PathVariable Long id, Principal principal) {
        String username = principal.getName();
        reminderService.deleteReminder(id, username);
        return ResponseEntity.noContent().build();
    }
}
package se.storkforge.petconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import se.storkforge.petconnect.dto.ReminderInputDTO;
import se.storkforge.petconnect.dto.ReminderResponseDTO;
import se.storkforge.petconnect.service.ReminderService;

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
    public ResponseEntity<Void> createReminder(@RequestBody ReminderInputDTO reminderInputDTO, Principal principal) {
        String username = principal.getName();
        reminderService.createReminder(reminderInputDTO, username);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}") // Vi vet inte ID:t här, men det är standard för 201 Created
                .buildAndExpand()
                .toUri();

        return ResponseEntity.created(location).build();
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
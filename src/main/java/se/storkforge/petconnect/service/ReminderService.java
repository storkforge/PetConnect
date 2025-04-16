package se.storkforge.petconnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.storkforge.petconnect.dto.ReminderInputDTO;
import se.storkforge.petconnect.dto.ReminderResponseDTO;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.Reminder;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.exception.PetNotFoundException;
import se.storkforge.petconnect.exception.UserNotFoundException;
import se.storkforge.petconnect.repository.PetRepository;
import se.storkforge.petconnect.repository.ReminderRepository;
import se.storkforge.petconnect.repository.UserRepository;
import se.storkforge.petconnect.util.OwnershipValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final OwnershipValidator ownershipValidator;

    @Autowired
    public ReminderService(ReminderRepository reminderRepository, UserRepository userRepository, PetRepository petRepository, OwnershipValidator ownershipValidator) {
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
        this.petRepository = petRepository;
        this.ownershipValidator = ownershipValidator;
    }

    public void createReminder(ReminderInputDTO reminderInputDTO, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        Pet pet = petRepository.findById(reminderInputDTO.getPetId())
                .orElseThrow(() -> new PetNotFoundException("Pet not found with ID: " + reminderInputDTO.getPetId()));

        Reminder reminder = new Reminder();
        reminder.setUser(user);
        reminder.setPet(pet);
        reminder.setTitle(reminderInputDTO.getTitle());
        reminder.setType(reminderInputDTO.getType());
        reminder.setScheduledDate(reminderInputDTO.getScheduledDate());
        reminder.setNotes(reminderInputDTO.getNotes());

        reminderRepository.save(reminder);
    }

    public List<ReminderResponseDTO> getUpcomingReminders(String username, LocalDateTime from, LocalDateTime to) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        List<Reminder> reminders = reminderRepository.findByUserAndScheduledDateBetween(user, from, to);
        return reminders.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public void deleteReminder(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found with ID: " + id));

        // Ägarskapsvalidering för Reminder
        if (reminder.getUser() == null || !reminder.getUser().getUsername().equals(username)) {
            throw new SecurityException("You do not have permission to delete this reminder.");
        }

        reminderRepository.deleteById(id);
    }

    private ReminderResponseDTO convertToResponseDTO(Reminder reminder) {
        return new ReminderResponseDTO(
                reminder.getId(),
                reminder.getTitle(),
                reminder.getPet().getName(),
                reminder.getType(),
                reminder.getScheduledDate(),
                reminder.getNotes()
        );
    }
}
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

    public ReminderResponseDTO createReminder(ReminderInputDTO reminderInputDTO, String username) {
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

        Reminder savedReminder = reminderRepository.save(reminder);
        return convertToResponseDTO(savedReminder); // Convert and return the DTO
    }

    // Ändrad getUpcomingReminders-metod
    public List<ReminderResponseDTO> getUpcomingReminders(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        List<Reminder> reminders = reminderRepository.findByUserOrderByScheduledDateAsc(user); // Hämta alla sorterade
        return reminders.stream()
                .filter(reminder -> reminder.getScheduledDate().isAfter(LocalDateTime.now())) // Filtrera på framtida
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public void deleteReminder(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found with ID: " + id));

        // Ägarskapsvalidering för Reminder
        ownershipValidator.validateOwnership(reminder.getPet(), username);

        reminderRepository.deleteById(id);
    }

    public ReminderResponseDTO convertToResponseDTO(Reminder reminder) {
        return new ReminderResponseDTO(
                reminder.getId(),
                reminder.getPet().getId(), // Get petId from the Pet entity
                reminder.getTitle(),
                reminder.getPet().getName(),
                reminder.getType(),
                reminder.getScheduledDate(),
                reminder.getNotes()
        );
    }
}
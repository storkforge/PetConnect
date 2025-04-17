package se.storkforge.petconnect.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private OwnershipValidator ownershipValidator;

    @InjectMocks
    private ReminderService reminderService;

    @Test
    void createReminder_shouldSaveNewReminder_whenUserAndPetExist() {
        // Arrange
        String username = "testuser";
        Long petId = 1L;
        ReminderInputDTO inputDTO = new ReminderInputDTO();
        inputDTO.setPetId(petId);
        inputDTO.setTitle("Vaccination");
        inputDTO.setType("Vet Visit");
        inputDTO.setScheduledDate(LocalDateTime.now().plusDays(7));
        inputDTO.setNotes("Annual vaccination");

        User user = new User();
        user.setUsername(username);
        Pet pet = new Pet();
        pet.setId(petId);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));
        when(reminderRepository.save(any(Reminder.class))).thenReturn(new Reminder()); // Returnera en dummy

        // Act
        reminderService.createReminder(inputDTO, username);

        // Assert
        verify(reminderRepository, times(1)).save(any(Reminder.class));
    }

    @Test
    void createReminder_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        // Arrange
        String username = "nonexistentuser";
        ReminderInputDTO inputDTO = new ReminderInputDTO();

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> reminderService.createReminder(inputDTO, username));
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    void createReminder_shouldThrowPetNotFoundException_whenPetDoesNotExist() {
        // Arrange
        String username = "testuser";
        Long petId = 99L;
        ReminderInputDTO inputDTO = new ReminderInputDTO();
        inputDTO.setPetId(petId);

        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(petRepository.findById(petId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PetNotFoundException.class, () -> reminderService.createReminder(inputDTO, username));
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    void getUpcomingReminders_shouldReturnListOfReminders_forGivenUserAndTimeRange() {
        // Arrange
        String username = "testuser";
        User user = new User();
        user.setUsername(username);
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = from.plusDays(7);

        Pet pet1 = new Pet();
        pet1.setName("Doggo");
        Pet pet2 = new Pet();
        pet2.setName("Catto");

        Reminder reminder1 = new Reminder(user, pet1, "Walk", "Activity", from.plusDays(1), "Morning walk");
        Reminder reminder2 = new Reminder(user, pet2, "Feed", "Care", from.plusDays(3), "Evening feed");
        List<Reminder> reminders = Arrays.asList(reminder1, reminder2);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(reminderRepository.findByUserAndScheduledDateBetween(user, from, to)).thenReturn(reminders);

        // Act
        List<ReminderResponseDTO> responseDTOs = reminderService.getUpcomingReminders(username, from, to);

        // Assert
        assertEquals(2, responseDTOs.size());
        assertEquals("Walk", responseDTOs.get(0).getTitle());
        assertEquals("Doggo", responseDTOs.get(0).getPetName());
        assertEquals("Feed", responseDTOs.get(1).getTitle());
        assertEquals("Catto", responseDTOs.get(1).getPetName());
    }

    @Test
    void getUpcomingReminders_shouldReturnEmptyList_whenNoRemindersFound() {
        // Arrange
        String username = "testuser";
        User user = new User();
        user.setUsername(username);
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = from.plusDays(7);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(reminderRepository.findByUserAndScheduledDateBetween(user, from, to)).thenReturn(List.of());

        // Act
        List<ReminderResponseDTO> responseDTOs = reminderService.getUpcomingReminders(username, from, to);

        // Assert
        assertTrue(responseDTOs.isEmpty());
    }

    @Test
    void deleteReminder_shouldDeleteReminder_whenUserOwnsIt() {
        // Arrange
        Long reminderId = 1L;
        String username = "owner";
        User owner = new User();
        owner.setUsername(username);
        Reminder reminder = new Reminder();
        reminder.setId(reminderId);
        reminder.setUser(owner);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(owner));
        when(reminderRepository.findById(reminderId)).thenReturn(Optional.of(reminder));

        // Act
        reminderService.deleteReminder(reminderId, username);

        // Assert
        verify(reminderRepository, times(1)).deleteById(reminderId);
    }

    @Test
    void deleteReminder_shouldThrowSecurityException_whenUserDoesNotOwnReminder() {
        // Arrange
        Long reminderId = 1L;
        String ownerUsername = "owner";
        String otherUsername = "attacker";
        User owner = new User();
        owner.setUsername(ownerUsername);
        User attacker = new User();
        attacker.setUsername(otherUsername);
        Reminder reminder = new Reminder();
        reminder.setId(reminderId);
        reminder.setUser(owner);

        when(userRepository.findByUsername(otherUsername)).thenReturn(Optional.of(attacker));
        when(reminderRepository.findById(reminderId)).thenReturn(Optional.of(reminder));

        // Act & Assert
        assertThrows(SecurityException.class, () -> reminderService.deleteReminder(reminderId, otherUsername));
        verify(reminderRepository, never()).deleteById(reminderId);
    }

    @Test
    void deleteReminder_shouldThrowIllegalArgumentException_whenReminderNotFound() {
        // Arrange
        Long reminderId = 1L;
        String username = "testuser";
        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(reminderRepository.findById(reminderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> reminderService.deleteReminder(reminderId, username));
        verify(reminderRepository, never()).deleteById(reminderId);
    }
}
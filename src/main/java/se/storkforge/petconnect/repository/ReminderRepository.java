package se.storkforge.petconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import se.storkforge.petconnect.entity.Reminder;
import se.storkforge.petconnect.entity.User;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByUserAndScheduledDateBetween(User user, LocalDateTime from, LocalDateTime to);

    // Optionally, a method to find the nearest upcoming reminders
    List<Reminder> findByUserOrderByScheduledDateAsc(User user);

    // Additional methods can be added here if needed, e.g.,
    // List<Reminder> findByPet(Pet pet);
    // List<Reminder> findByScheduledDate(LocalDateTime scheduledDate);
}
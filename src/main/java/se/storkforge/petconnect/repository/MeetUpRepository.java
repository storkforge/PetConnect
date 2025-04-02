package se.storkforge.petconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.storkforge.petconnect.entity.MeetUp;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetUpRepository extends JpaRepository<MeetUp, Long> {

    List<MeetUp> findByLocationContaining(String location);
    List<MeetUp> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);
}

package se.storkforge.petconnect.repository;

import entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Lägg till denna rad

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
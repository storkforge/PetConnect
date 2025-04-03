package se.storkforge.petconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.storkforge.petconnect.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
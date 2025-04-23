package se.storkforge.petconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.storkforge.petconnect.entity.Role;
import se.storkforge.petconnect.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByRolesContaining(Role role);

    long countByRolesContaining(Role role);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.pets WHERE u.id = :id")
    Optional<User> findByIdWithPets(@Param("id") Long id);
}
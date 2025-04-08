package se.storkforge.petconnect.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import se.storkforge.petconnect.entity.Role;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.repository.RoleRepository;
import se.storkforge.petconnect.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean

    // initializes the database with test data at launch of the application
    CommandLineRunner initData(UserRepository userRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            // Create roles if not present
            Role userRole = createRoleIfNotFound(roleRepository, "ROLE_USER");
            Role premiumRole = createRoleIfNotFound(roleRepository, "ROLE_PREMIUM");

            // Create test user
            if (userRepository.findByUsername("testuser").isEmpty()) {
                User user = new User();
                user.setUsername("testuser");
                user.setEmail("test@example.com");
                user.setPassword(passwordEncoder.encode("password"));
                user.setRoles(Set.of(userRole));
                userRepository.save(user);
                System.out.println("Created user: " + user.getUsername()); // log user existing
            } else { System.out.println("User already exists"); }

            // Create premium user
            if (userRepository.findByUsername("premiumuser").isEmpty()) {
                User user = new User();
                user.setUsername("premiumuser");
                user.setEmail("premium@example.com");
                user.setPassword(passwordEncoder.encode("password"));
                user.setRoles(Set.of(premiumRole));
                userRepository.save(user);
            }
        };
    }



    // Helper method using lambda to create a role if it doesn't exist
    private Role createRoleIfNotFound(RoleRepository repo, String roleName) {
        return repo.findByName(roleName)
                .orElseGet(() -> repo.save(new Role(roleName)));
    }
}
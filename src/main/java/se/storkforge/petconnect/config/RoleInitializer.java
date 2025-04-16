package se.storkforge.petconnect.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.storkforge.petconnect.entity.Role;
import se.storkforge.petconnect.repository.RoleRepository;
import se.storkforge.petconnect.security.Roles;

import java.util.List;

@Component
public class RoleInitializer {

    private static final Logger log = LoggerFactory.getLogger(RoleInitializer.class);
    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void insertDefaultRoles() {
        List<String> requiredRoles = List.of(
                Roles.USER,
                Roles.PREMIUM,
                Roles.ADMIN
        );

        for (String roleName : requiredRoles) {
            roleRepository.findByName(roleName)
                    .orElseGet(() -> {
                        log.info("Inserting missing role: {}", roleName);
                        Role role = new Role();
                        role.setName(roleName);
                        return roleRepository.save(role);
                    });
        }

        log.info("Default roles ensured.");
    }
}

package se.storkforge.petconnect.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import se.storkforge.petconnect.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import se.storkforge.petconnect.dto.RegistrationForm;
import se.storkforge.petconnect.entity.Role;
import se.storkforge.petconnect.repository.RoleRepository;
import se.storkforge.petconnect.repository.UserRepository;
import org.springframework.ui.Model;

import java.util.Optional;
import java.util.Set;

@Controller
public class RegistrationController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);

    public RegistrationController(UserRepository userRepository,
                                  PasswordEncoder passwordEncoder,
                                  RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                                      BindingResult bindingResult,
                                      Model model) {
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors during registration: {}", bindingResult.getAllErrors());
            return "auth/register";
        }

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match.");
            log.warn("Password mismatch for user: {}", form.getUsername());
            return "auth/register";
        }

        Optional<User> existingUser = userRepository.findByUsername(form.getUsername());
        if (existingUser.isPresent()) {
            bindingResult.rejectValue("username", "error.username", "Username already exists.");
            log.warn("Attempt to register with existing username: {}", form.getUsername());
            return "auth/register";
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = new User();
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRoles(Set.of(userRole));

        userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());

        return "redirect:/login?registered";
    }

}

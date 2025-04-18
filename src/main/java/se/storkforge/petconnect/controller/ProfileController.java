package se.storkforge.petconnect.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.repository.UserRepository;

import java.util.Optional;

@Controller
public class ProfileController {
    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public String dashboard(Model model, Authentication authentication) {
        String username = "Guest";

        if (authentication != null) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails userDetails) {
                username = userDetails.getUsername();
            } else if (principal instanceof OAuth2User oauthUser) {
                username = oauthUser.getAttribute("name"); // or "email", etc
            }
        }
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            Long id = user.get().getId();
            model.addAttribute("id", user.get().getId());
        }

        model.addAttribute("username", username);
        return "profile";
    }

}

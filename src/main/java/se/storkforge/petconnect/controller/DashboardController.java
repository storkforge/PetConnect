package se.storkforge.petconnect.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import se.storkforge.petconnect.dto.ReminderResponseDTO;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.service.aiService.RecommendationService;
import se.storkforge.petconnect.service.ReminderService;
import se.storkforge.petconnect.service.UserService;

import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {

    private final RecommendationService recommendationService;
    private final UserService userService;
    private final ReminderService reminderService;

    public DashboardController(RecommendationService recommendationService,
                               UserService userService,
                               ReminderService reminderService) {
        this.recommendationService = recommendationService;
        this.userService = userService;
        this.reminderService = reminderService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        String username = "Guest";
        Optional<User> userOptional = Optional.empty();

        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            username = getUsernameFromPrincipal(principal);
            userOptional = userService.getUserByUsername(username);
        }

        model.addAttribute("username", username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            setupUserDashboard(model, user);
        } else {
            setupGuestDashboard(model);
        }

        return "dashboard";
    }

    private String getUsernameFromPrincipal(Object principal) {
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        } else if (principal instanceof OAuth2User oauthUser) {
            return oauthUser.getAttribute("name");
        }
        return "Guest";
    }

    private void setupUserDashboard(Model model, User user) {
        // Get upcoming reminders
        List<ReminderResponseDTO> upcomingReminders =
                reminderService.getUpcomingReminders(user.getUsername())
                        .stream()
                        .limit(3)
                        .toList();
        model.addAttribute("upcomingReminders", upcomingReminders);

        // Get care tip based on user's pets
        String petType = determinePetType(user);
        String careTip = recommendationService.generateCareTip(petType);
        model.addAttribute("careTip", careTip);

        // Add pet recommendation if needed
        String recommendation = recommendationService.generateRecommendation(user);
        model.addAttribute("recommendation", recommendation);
    }

    private void setupGuestDashboard(Model model) {
        model.addAttribute("upcomingReminders", List.of());
        model.addAttribute("careTip", "Log in to see reminders and care tips.");
    }

    private String determinePetType(User user) {
        // First try to get from user's pets
        if (user.getPets() != null && !user.getPets().isEmpty()) {
            return user.getPets().getFirst().getSpecies(); // Default to first pet's species
        }

        // Fallback logic if no pets
        return "pet"; // Generic fallback
    }
}
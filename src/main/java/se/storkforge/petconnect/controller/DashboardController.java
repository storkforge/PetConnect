package se.storkforge.petconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import se.storkforge.petconnect.dto.ReminderResponseDTO; // Importera ReminderResponseDTO
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.service.RecommendationService;
import se.storkforge.petconnect.service.ReminderService;
import se.storkforge.petconnect.service.UserService;

import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {

    private final RecommendationService recommendationService;
    private final UserService userService;
    private final ReminderService reminderService;

    @Autowired
    public DashboardController(RecommendationService recommendationService, UserService userService, ReminderService reminderService) {
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

            if (principal instanceof UserDetails userDetails) {
                username = userDetails.getUsername();
                userOptional = userService.getUserByUsername(username);
            } else if (principal instanceof OAuth2User oauthUser) {
                username = oauthUser.getAttribute("name"); // or "email", etc
                // För OAuth2-användare kan du behöva ett annat sätt att hämta User-entiteten
                // baserat på t.ex. email eller sub från OAuth2-providern.
                userOptional = userService.getUserByUsername(username); // Anpassa detta!
            }
        }

        model.addAttribute("username", username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Steg 6: Hämta och visa kommande påminnelser
            List<ReminderResponseDTO> upcomingReminders = // Ändrad typ här
                    reminderService.getUpcomingReminders(username).stream().limit(3).toList();
            model.addAttribute("upcomingReminders", upcomingReminders);

            // Steg 7: Hämta och visa vårdtips
            String petType = "cat"; // Exempel: Hårdkodat. Behöver dynamisk hämtning baserat på user.getPets()
            String careTip = recommendationService.generateCareTip(petType);
            model.addAttribute("careTip", careTip);
        } else {
            model.addAttribute("upcomingReminders", List.of());
            model.addAttribute("careTip", "Logga in för att se påminnelser och vårdtips.");
        }

        return "dashboard";
    }
}
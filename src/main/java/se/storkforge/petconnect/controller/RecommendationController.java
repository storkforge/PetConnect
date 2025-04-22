package se.storkforge.petconnect.controller;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.service.UserService;
import se.storkforge.petconnect.service.aiService.AiToyAndFoodRecommendationService;
import se.storkforge.petconnect.service.aiService.RecommendationService;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Controller
@RequestMapping("/recommendations")
public class RecommendationController {
    AiToyAndFoodRecommendationService aiToyAndFoodRecommendationService;
    RecommendationService recommendationService;
    UserService userService;

    public RecommendationController(UserService userService,
                                    RecommendationService recommendationService,
                                    AiToyAndFoodRecommendationService aiToyAndFoodRecommendationService) {
        this.userService = userService;
        this.recommendationService = recommendationService;
        this.aiToyAndFoodRecommendationService = aiToyAndFoodRecommendationService;
    }


    @GetMapping("/foodandtoys")
    public String foodAndToyRecommendations(Principal principal, Model model) {
        if (principal == null) {
            throw new UsernameNotFoundException("User must be logged in to view recommendations");
        }

        User loggedInUser = userService.getUserByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Logged in user not found"));

        model.addAttribute("role", loggedInUser.hasRole("ROLE_PREMIUM") || loggedInUser.hasRole("ROLE_ADMIN"));

        if (loggedInUser.hasRole("ROLE_PREMIUM") || loggedInUser.hasRole("ROLE_ADMIN")) {
            List<Pet> pets = loggedInUser.getPets();
            if (pets != null && !pets.isEmpty()) {
                Map<String, String> foodRecommendations = new HashMap<>();
                Map<String, String> toyRecommendations = new HashMap<>();
                for (Pet pet : pets) {
                    foodRecommendations.put(pet.getName(), aiToyAndFoodRecommendationService.getFoodRecommendation(pet.getAge(), pet.getSpecies()));
                    toyRecommendations.put(pet.getName(), aiToyAndFoodRecommendationService.getToyRecommendation(pet.getAge(), pet.getSpecies()));
                }
                model.addAttribute("pets", pets);
                model.addAttribute("foodRecommendations", foodRecommendations);
                model.addAttribute("toyRecommendations", toyRecommendations);
            }
        }
        return "foodandtoys";
    }
}


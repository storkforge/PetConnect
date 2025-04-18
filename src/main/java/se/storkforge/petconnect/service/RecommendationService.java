package se.storkforge.petconnect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    private final PetService petService;
    private final AiRecommendationExecutor aiExecutor;

    public RecommendationService(PetService petService, AiRecommendationExecutor aiExecutor) {
        this.petService = petService;
        this.aiExecutor = aiExecutor;
    }

    public String generateRecommendation(User user) {
        if (user == null) {
            logger.warn("Attempted to generate recommendation for null user");
            return "User information is missing. Cannot generate recommendation.";
        }

        Pageable pageable = PageRequest.of(0, 100);
        Page<Pet> petsPage = petService.getAllPets(pageable, null);

        if (petsPage.isEmpty()) {
            return "No available pets to recommend at this time.";
        }

        List<Pet> availablePets = petsPage.getContent();
        String prompt = createPrompt(user, availablePets);

        try {
            return aiExecutor.callAi(new Prompt(prompt));
        } catch (Exception e) {
            logger.error("AI recommendation failed for user {}", user.getUsername(), e);
            return createFallbackRecommendation(availablePets);
        }
    }

    public String generateCareTip(String petType) {
        try {
            String prompt = "Provide one useful health care tip for " + petType +
                    ". Keep it under 150 characters.";
            return aiExecutor.callAi(new Prompt(prompt));
        } catch (Exception e) {
            logger.error("Failed to generate care tip for {}", petType, e);
            return getDefaultCareTip(petType);
        }
    }

    private String createPrompt(User user, List<Pet> pets) {
        return String.format(
                "Generate a personalized pet recommendation for user %s (%s). " +
                        "Available pets: %s",
                user.getUsername(),
                user.getEmail(),
                pets.stream()
                        .map(pet -> String.format("%s the %s (Age: %d, Location: %s)",
                                pet.getName(), pet.getSpecies(), pet.getAge(), pet.getLocation()))
                        .collect(Collectors.joining(", "))
        );
    }

    private String createFallbackRecommendation(List<Pet> pets) {
        Pet fallbackPet = pets.getFirst();
        return String.format(
                "Our recommendation engine is unavailable. Based on basic matching, %s the %s might be a good fit.",
                fallbackPet.getName(),
                fallbackPet.getSpecies()
        );
    }

    private String getDefaultCareTip(String petType) {
        return switch (petType.toLowerCase()) {
            case "cat" -> "Regular brushing helps reduce hairballs in cats.";
            case "dog" -> "Daily walks are essential for a dog's physical and mental health.";
            default -> "Regular veterinary check-ups are important for all pets.";
        };
    }
}
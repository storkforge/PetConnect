package se.storkforge.petconnect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    private final PetService petService;
    private final AiRecommendationExecutor aiExecutor;

    public RecommendationService(PetService petService, AiRecommendationExecutor aiExecutor) {
        this.petService = petService;
        this.aiExecutor = aiExecutor;
    }

    private static final String RECOMMENDATION_TEMPLATE = """
     Based on the following user information and available pets, provide a personalized pet recommendation.

     User Details:
     - Username: {username}
     - Email: {email}

     Available Pets:
     {pets}

     Please recommend the most suitable pet for this user.
     Provide a brief explanation for your recommendation.
     """;

    public String generateRecommendation(User user) {
        if (user == null) {
            logger.warn("Attempted to generate recommendation for a null user");
            return "User information is missing. Cannot generate recommendation.";
        }

        logger.info("Generating AI recommendation for user {}", user.getUsername());
        List<Pet> availablePets = getAvailablePets();
        if (availablePets.isEmpty()) {
            return "No available pets to recommend at this time.";
        }

        Map<String, Object> promptVariables = createPromptVariables(user, availablePets);
        Prompt prompt = createPrompt(promptVariables);

        try {
            return aiExecutor.callAi(prompt); // <-- uses the executor here
        } catch (RuntimeException e) {
            return fallback(e, user); // Call fallback on exception
        }
    }

    private List<Pet> getAvailablePets() {
        Pageable pageable = PageRequest.of(0, 100); // Fetch first 100 pets
        return petService.getAllPets(pageable, null) // Pass null for PetFilter
                .stream()
                .filter(Pet::isAvailable)
                .toList();
    }

    private Map<String, Object> createPromptVariables(User user, List<Pet> availablePets) {
        Map<String, Object> promptVariables = new HashMap<>();

        String petsFormatted = formatPetsList(availablePets);

        promptVariables.put("username", user.getUsername());
        promptVariables.put("email", user.getEmail());
        promptVariables.put("pets", petsFormatted);

        return promptVariables;
    }

    private String formatPetsList(List<Pet> pets) {
        return pets.stream()
                .map(pet -> String.format("- %s (%s), Age: %d, Location: %s",
                        pet.getName(), pet.getSpecies(), pet.getAge(), pet.getLocation()))
                .reduce("", (a, b) -> a + "\n" + b);
    }

    private Prompt createPrompt(Map<String, Object> promptVariables) {
        PromptTemplate promptTemplate = new PromptTemplate(RECOMMENDATION_TEMPLATE);
        return promptTemplate.create(promptVariables);
    }

    public String fallback(RuntimeException e, User user) {
        logger.error("AI recommendation failed after retries. User: {}", user != null ? user.getUsername() : "null", e);
        return "Our recommendation engine is currently unavailable. Please try again later.";
    }
}
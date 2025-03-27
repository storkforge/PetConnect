package se.storkforge.petconnect.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationService {

    private final ChatClient chatClient;
    private final PetService petService;

    public RecommendationService(ChatClient chatClient, PetService petService) {
        this.chatClient = chatClient;
        this.petService = petService;
    }

    public String generateRecommendation(User user) {
        List<Pet> availablePets = petService.getAllPets().stream()
                .filter(Pet::isAvailable)
                .toList();

        Map<String, Object> promptVariables = new HashMap<>();
        promptVariables.put("user", user);
        promptVariables.put("pets", availablePets);

        // Using simple variable substitution without loops
        String template = """
            Based on the following user information and available pets, provide a personalized pet recommendation.

            User Details:
            - Username: {username}
            - Email: {email}

            Available Pets:
            {pets}

            Please recommend the most suitable pet for this user.
            Provide a brief explanation for your recommendation.
            """;

        // Pre-format the pets list
        String petsFormatted = availablePets.stream()
                .map(pet -> String.format("- %s (%s), Age: %d, Location: %s",
                        pet.getName(), pet.getSpecies(), pet.getAge(), pet.getLocation()))
                .reduce("", (a, b) -> a + "\n" + b);

        promptVariables.put("username", user.getUsername());
        promptVariables.put("email", user.getEmail());
        promptVariables.put("pets", petsFormatted);

        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(promptVariables);
        return chatClient.prompt(prompt).call().content();
    }
}
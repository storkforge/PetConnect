package se.storkforge.petconnect.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
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

    public RecommendationService(ChatModel chatClient, PetService petService) {
        this.chatClient = (ChatClient) chatClient;
        this.petService = petService;
    }

    public String generateRecommendation(User user) {
        List<Pet> availablePets = petService.getAllPets().stream()
                .filter(Pet::isAvailable)
                .toList();

        Map<String, Object> promptVariables = new HashMap<>();
        promptVariables.put("user", user);
        promptVariables.put("pets", availablePets);

        PromptTemplate promptTemplate = new PromptTemplate("""
            Based on the following user information and available pets, provide a personalized pet recommendation.

            User Details:
            - Username: {user.username}
            - Email: {user.email}

            Available Pets:
            {#each pets}
            - {name} ({species}), Age: {age}, Location: {location}
            {/each}

            Please recommend the most suitable pet for this user, considering factors like species, age, and location proximity.
            Provide a brief explanation for your recommendation.
            """);

        Prompt prompt = promptTemplate.create(promptVariables);
        return chatClient.prompt(prompt).call().content();
    }
}
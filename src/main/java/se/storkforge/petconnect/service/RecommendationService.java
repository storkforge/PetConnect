package se.storkforge.petconnect.service;

import org.springframework.ai.chat.client.ChatClient;
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

    private final ChatClient chatClient;
    private final PetService petService;

    public RecommendationService(ChatClient chatClient, PetService petService) {
        this.chatClient = chatClient;
        this.petService = petService;
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
            try {
               List<Pet> availablePets = getAvailablePets();
                if (availablePets.isEmpty()) {
                    return "No available pets to recommend at this time.";
                }

                Map<String, Object> promptVariables = createPromptVariables(user, availablePets);
                Prompt prompt = createPrompt(promptVariables);
                return chatClient.prompt(prompt).call().content();
           } catch (Exception e) {
                // Log the error
                // Import org.slf4j.Logger and org.slf4j.LoggerFactory to use this
                // private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);
                // logger.error("Error generating pet recommendation", e);
                return "Unable to generate recommendation at this time. Please try again later.";
            }
        }

    private List<Pet> getAvailablePets() {
             Pageable pageable = PageRequest.of(0, 100);
             return petService.getAllPets().stream()
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
}
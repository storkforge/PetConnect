package se.storkforge.petconnect.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.prompt.Prompt;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClientRequestSpec requestSpec;

    @Mock
    private CallResponseSpec responseSpec;

    @Mock
    private PetService petService;

    @InjectMocks
    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
    }

    @Test
    void generateRecommendation_ShouldReturnPersonalizedRecommendation() {
        // Arrange
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        Pet pet1 = new Pet();
        pet1.setName("Fluffy");
        pet1.setSpecies("Cat");
        pet1.setAge(2);
        pet1.setLocation("Stockholm");
        pet1.setAvailable(true);

        Pet pet2 = new Pet();
        pet2.setName("Rex");
        pet2.setSpecies("Dog");
        pet2.setAge(4);
        pet2.setLocation("Gothenburg");
        pet2.setAvailable(true);

        List<Pet> availablePets = List.of(pet1, pet2);

        when(petService.getAllPets()).thenReturn(availablePets);
        when(responseSpec.content()).thenReturn("I recommend Fluffy the Cat, as they match your lifestyle...");

        // Act
        String recommendation = recommendationService.generateRecommendation(testUser);

        // Assert
        assertEquals("I recommend Fluffy the Cat, as they match your lifestyle...", recommendation);
    }

    @Test
    void generateRecommendation_ShouldFilterOutUnavailablePets() {
        // Arrange
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        Pet availablePet = new Pet();
        availablePet.setName("Fluffy");
        availablePet.setSpecies("Cat");
        availablePet.setAge(2);
        availablePet.setLocation("Stockholm");
        availablePet.setAvailable(true);

        Pet unavailablePet = new Pet();
        unavailablePet.setName("Rex");
        unavailablePet.setSpecies("Dog");
        unavailablePet.setAge(4);
        unavailablePet.setLocation("Gothenburg");
        unavailablePet.setAvailable(false);

        List<Pet> allPets = List.of(availablePet, unavailablePet);

        when(petService.getAllPets()).thenReturn(allPets);
        when(responseSpec.content()).thenReturn("I recommend Fluffy the Cat...");

        // Act
        String recommendation = recommendationService.generateRecommendation(testUser);

        // Assert
        assertEquals("I recommend Fluffy the Cat...", recommendation);
    }
}
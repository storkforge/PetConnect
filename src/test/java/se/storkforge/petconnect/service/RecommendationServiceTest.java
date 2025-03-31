package se.storkforge.petconnect.service;

import org.junit.jupiter.api.Assertions;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    // Ta bort stubbningarna från setUp()
    @BeforeEach
    void setUp() {
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
        Page<Pet> mockPage = new PageImpl<>(availablePets);
        when(petService.getAllPets(any(Pageable.class))).thenReturn(mockPage);

        // Lägg till stubbningar direkt i testet
        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
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
        Page<Pet> mockPage = new PageImpl<>(allPets);
        when(petService.getAllPets(any(Pageable.class))).thenReturn(mockPage);

        // Lägg till stubbningar direkt i testet
        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("I recommend Fluffy the Cat...");

        // Act
        String recommendation = recommendationService.generateRecommendation(testUser);

        // Assert
        assertEquals("I recommend Fluffy the Cat...", recommendation);
    }

    @Test
    void generateRecommendation_ShouldHandleNoPetsAvailable() {
        // Arrange
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        // No available pets
        List<Pet> noPets = List.of();
        Page<Pet> mockPage = new PageImpl<>(noPets);
        when(petService.getAllPets(any(Pageable.class))).thenReturn(mockPage);

        // Act
        String actualResult = recommendationService.generateRecommendation(testUser);

        // Assert
        Assertions.assertEquals("No available pets to recommend at this time.", actualResult);
    }

    @Test
    void generateRecommendation_ShouldHandleNullUser() {
        String result = recommendationService.generateRecommendation(null);
        assertEquals("User information is missing. Cannot generate recommendation.", result);
    }

    @Test
    void fallback_ShouldReturnFallbackMessage() {
        User user = new User();
        user.setUsername("testuser");

        RuntimeException ex = new RuntimeException("Simulated failure");
        String result = recommendationService.fallback(ex, user);

        assertEquals("Our recommendation engine is currently unavailable. Please try again later.", result);
    }

    @Test
    void shouldFallbackAfterRetries() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        Pet pet = new Pet("Fluffy", "Cat", true, 2, "Owner", "Stockholm");
        Pageable pageable = PageRequest.of(0, 100);
        when(petService.getAllPets(pageable)).thenReturn(new PageImpl<>(List.of(pet)));

        // Simulate AI failure
        when(chatClient.prompt(any(Prompt.class))).thenThrow(new RuntimeException("AI down"));

        // Act
        String result = recommendationService.generateRecommendation(user);

        // Assert
        assertEquals("Our recommendation engine is currently unavailable. Please try again later.", result);
    }
}
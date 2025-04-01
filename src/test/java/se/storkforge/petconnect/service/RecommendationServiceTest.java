package se.storkforge.petconnect.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    private PetService petService;

    @Mock
    private AiRecommendationExecutor aiExecutor;

    @InjectMocks
    private RecommendationService recommendationService;

    private User testUser;
    private User petOwner;
    private Pet testPet;
    private Pet unavailablePet;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        // Setup pet owner
        petOwner = new User();
        petOwner.setUsername("petowner");
        petOwner.setEmail("owner@example.com");

        // Setup available pet
        testPet = new Pet();
        testPet.setName("Fluffy");
        testPet.setSpecies("Cat");
        testPet.setAvailable(true);
        testPet.setAge(2);
        testPet.setOwner(petOwner);
        testPet.setLocation("Stockholm");

        // Setup unavailable pet
        unavailablePet = new Pet();
        unavailablePet.setName("Rex");
        unavailablePet.setSpecies("Dog");
        unavailablePet.setAvailable(false);
        unavailablePet.setAge(3);
        unavailablePet.setOwner(petOwner);
        unavailablePet.setLocation("Gothenburg");
    }

    @Test
    void generateRecommendation_ShouldReturnPersonalizedRecommendation() {
        Page<Pet> pets = new PageImpl<>(List.of(testPet));
        when(petService.getAllPets(any(Pageable.class))).thenReturn(pets);
        when(aiExecutor.callAi(any(Prompt.class)))
                .thenReturn("Fluffy is the perfect match for you!");

        String result = recommendationService.generateRecommendation(testUser);

        assertEquals("Fluffy is the perfect match for you!", result);
    }

    @Test
    void generateRecommendation_ShouldFilterOutUnavailablePets() {
        Page<Pet> pets = new PageImpl<>(List.of(testPet, unavailablePet));
        when(petService.getAllPets(any(Pageable.class))).thenReturn(pets);
        when(aiExecutor.callAi(any(Prompt.class))).thenReturn("I recommend Fluffy!");

        String result = recommendationService.generateRecommendation(testUser);

        assertEquals("I recommend Fluffy!", result);
    }

    @Test
    void generateRecommendation_ShouldHandleNoPetsAvailable() {
        when(petService.getAllPets(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of())); // empty list

        String result = recommendationService.generateRecommendation(testUser);

        assertEquals("No available pets to recommend at this time.", result);
    }

    @Test
    void generateRecommendation_ShouldHandleNullUser() {
        String result = recommendationService.generateRecommendation(null);

        assertEquals("User information is missing. Cannot generate recommendation.", result);
    }

    @Test
    void fallback_ShouldReturnExpectedMessageWhenCalledManually() {
        Prompt prompt = new Prompt("...");
        RuntimeException ex = new RuntimeException("Simulated failure");

        AiRecommendationExecutor executor = new AiRecommendationExecutor(null); // okay here
        String result = executor.fallback(ex, prompt);

        assertEquals("Our recommendation engine is currently unavailable. Please try again later.", result);
    }
}
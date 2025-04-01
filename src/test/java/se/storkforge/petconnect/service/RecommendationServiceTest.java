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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private PetService petService;

    @Mock
    private AiRecommendationExecutor aiExecutor;

    @InjectMocks
    private RecommendationService recommendationService;

    private User testUser;
    private Pet testPet;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testPet = new Pet();
        testPet.setName("Fluffy");
        testPet.setSpecies("Cat");
        testPet.setAvailable(true);
        testPet.setAge(2);
        testPet.setLocation("Stockholm");

        // Reset mocks before each test
        reset(petService, aiExecutor);
    }

    @Test
    void generateRecommendation_ShouldReturnPersonalizedRecommendation() {
        // Arrange
        Pageable expectedPageable = PageRequest.of(0, 100);
        Page<Pet> mockPage = new PageImpl<>(List.of(testPet));

        // Properly mock the PetService behavior
        doReturn(mockPage)
                .when(petService)
                .getAllPets(expectedPageable, null);

        when(aiExecutor.callAi(any(Prompt.class)))
                .thenReturn("Fluffy is the perfect match for you!");

        // Act
        String result = recommendationService.generateRecommendation(testUser);

        // Assert
        assertEquals("Fluffy is the perfect match for you!", result);
        verify(petService).getAllPets(expectedPageable, null);
        verify(aiExecutor).callAi(any(Prompt.class));
    }

    @Test
    void generateRecommendation_ShouldHandleNoPetsAvailable() {
        // Arrange
        Pageable expectedPageable = PageRequest.of(0, 100);
        Page<Pet> emptyPage = new PageImpl<>(List.of());

        doReturn(emptyPage)
                .when(petService)
                .getAllPets(expectedPageable, null);

        // Act
        String result = recommendationService.generateRecommendation(testUser);

        // Assert
        assertEquals("No available pets to recommend at this time.", result);
    }

    @Test
    void generateRecommendation_ShouldHandleNullUser() {
        // Act & Assert
        assertEquals("User information is missing. Cannot generate recommendation.",
                recommendationService.generateRecommendation(null));

        // Verify no interactions with mocks
        verifyNoInteractions(petService);
        verifyNoInteractions(aiExecutor);
    }
}
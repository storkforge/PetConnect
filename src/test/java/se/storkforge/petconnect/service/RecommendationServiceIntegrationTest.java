package se.storkforge.petconnect.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.annotation.EnableRetry;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EnableRetry
class RecommendationServiceIntegrationTest {

    @Autowired
    private RecommendationService recommendationService;

    @MockitoBean
    private ChatClient chatClient;

    @MockitoBean
    private PetService petService;

    @Test
    void shouldFallbackAfterRetries() {
        // Setup
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        Pet pet = new Pet("Fluffy", "Cat", true, 2, "Owner", "Stockholm");
        Pageable pageable = PageRequest.of(0, 10);

        when(petService.getAllPets(pageable))
                .thenReturn(new PageImpl<>(List.of(pet)));

        when(chatClient.prompt(any(Prompt.class)))
                .thenThrow(new RuntimeException("AI down"));

        // Act
        String result = recommendationService.generateRecommendation(user);

        // Assert
        assertEquals("Our recommendation engine is currently unavailable. Please try again later.", result);
    }
}

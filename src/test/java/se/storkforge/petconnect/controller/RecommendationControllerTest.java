package se.storkforge.petconnect.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.Role;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.service.UserService;
import se.storkforge.petconnect.service.aiService.AiToyAndFoodRecommendationService;
import se.storkforge.petconnect.service.aiService.RecommendationService;

import java.security.Principal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private RecommendationService recommendationService;

    @Mock
    private AiToyAndFoodRecommendationService aiToyAndFoodRecommendationService;

    @Mock
    private Principal principal;

    @Mock
    private Model model;

    @InjectMocks
    private RecommendationController recommendationController;

    private User testUser;
    private User premiumUser;
    private Pet testPet;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setRoles(Set.of(new Role("ROLE_USER")));

        premiumUser = new User();
        premiumUser.setUsername("premiumuser");
        premiumUser.setRoles(Set.of(new Role("ROLE_PREMIUM"), new Role(
                "ROLE_USER")));

        testPet = new Pet();
        testPet.setName("Fluffy");
        testPet.setAge(3);
        testPet.setSpecies("Dog");
        premiumUser.setPets(List.of(testPet));
    }

    @Test
    void foodAndToyRecommendations_shouldThrowExceptionWhenPrincipalIsNull() {
        assertThrows(UsernameNotFoundException.class,
                () -> recommendationController.foodAndToyRecommendations(null, model));
    }

    @Test
    void foodAndToyRecommendations_shouldThrowExceptionWhenUserNotFound() {
        when(principal.getName()).thenReturn("nonexistent");
        when(userService.getUserByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> recommendationController.foodAndToyRecommendations(principal, model));
    }

    @Test
    void foodAndToyRecommendations_shouldAddRoleAttributeForRegularUser() {
        when(principal.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));

        String viewName = recommendationController.foodAndToyRecommendations(principal, model);

        verify(model).addAttribute("role", false);
        assertEquals("foodandtoys", viewName);
    }

    @Test
    void foodAndToyRecommendations_shouldAddRoleAttributeForPremiumUser() {
        when(principal.getName()).thenReturn("premiumuser");
        when(userService.getUserByUsername("premiumuser")).thenReturn(Optional.of(premiumUser));

        String viewName = recommendationController.foodAndToyRecommendations(principal, model);

        verify(model).addAttribute("role", true);
        assertEquals("foodandtoys", viewName);
    }

    @Test
    void foodAndToyRecommendations_shouldNotAddRecommendationsForRegularUser() {
        when(principal.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));

        String viewName = recommendationController.foodAndToyRecommendations(principal, model);

        verify(model, never()).addAttribute(eq("pets"), any());
        verify(model, never()).addAttribute(eq("foodRecommendations"), any());
        verify(model, never()).addAttribute(eq("toyRecommendations"), any());
        assertEquals("foodandtoys", viewName);
    }

    @Test
    void foodAndToyRecommendations_shouldAddRecommendationsForPremiumUserWithPets() {
        when(principal.getName()).thenReturn("premiumuser");
        when(userService.getUserByUsername("premiumuser")).thenReturn(Optional.of(premiumUser));
        when(aiToyAndFoodRecommendationService.getFoodRecommendation(3, "Dog"))
                .thenReturn("Recommended food for Dog");
        when(aiToyAndFoodRecommendationService.getToyRecommendation(3, "Dog"))
                .thenReturn("Recommended toy for Dog");

        String viewName = recommendationController.foodAndToyRecommendations(principal, model);

        verify(model).addAttribute("role", true);
        verify(model).addAttribute("pets", List.of(testPet));

        // Verify food recommendations
        verify(model).addAttribute(eq("foodRecommendations"), argThat(map -> {
            Map<String, String> foodRecs = (Map<String, String>) map;
            return foodRecs.size() == 1 &&
                    foodRecs.get("Fluffy").equals("Recommended food for Dog");
        }));

        // Verify toy recommendations
        verify(model).addAttribute(eq("toyRecommendations"), argThat(map -> {
            Map<String, String> toyRecs = (Map<String, String>) map;
            return toyRecs.size() == 1 &&
                    toyRecs.get("Fluffy").equals("Recommended toy for Dog");
        }));

        assertEquals("foodandtoys", viewName);
    }

    @Test
    void foodAndToyRecommendations_shouldHandlePremiumUserWithNoPets() {
        premiumUser.setPets(Collections.emptyList());

        when(principal.getName()).thenReturn("premiumuser");
        when(userService.getUserByUsername("premiumuser")).thenReturn(Optional.of(premiumUser));

        String viewName = recommendationController.foodAndToyRecommendations(principal, model);

        verify(model).addAttribute("role", true);
        verify(model, never()).addAttribute(eq("pets"), any());
        verify(model, never()).addAttribute(eq("foodRecommendations"), any());
        verify(model, never()).addAttribute(eq("toyRecommendations"), any());
        assertEquals("foodandtoys", viewName);
    }

    @Test
    void foodAndToyRecommendations_shouldHandleMultiplePets() {
        Pet secondPet = new Pet();
        secondPet.setName("Whiskers");
        secondPet.setAge(2);
        secondPet.setSpecies("Cat");
        premiumUser.setPets(List.of(testPet, secondPet));

        when(principal.getName()).thenReturn("premiumuser");
        when(userService.getUserByUsername("premiumuser")).thenReturn(Optional.of(premiumUser));
        when(aiToyAndFoodRecommendationService.getFoodRecommendation(3, "Dog"))
                .thenReturn("Dog food");
        when(aiToyAndFoodRecommendationService.getToyRecommendation(3, "Dog"))
                .thenReturn("Dog toy");
        when(aiToyAndFoodRecommendationService.getFoodRecommendation(2, "Cat"))
                .thenReturn("Cat food");
        when(aiToyAndFoodRecommendationService.getToyRecommendation(2, "Cat"))
                .thenReturn("Cat toy");

        String viewName = recommendationController.foodAndToyRecommendations(principal, model);

        verify(model).addAttribute(eq("foodRecommendations"), argThat(map -> {
            Map<String, String> foodRecs = (Map<String, String>) map;
            return foodRecs.size() == 2 &&
                    foodRecs.get("Fluffy").equals("Dog food") &&
                    foodRecs.get("Whiskers").equals("Cat food");
        }));

        verify(model).addAttribute(eq("toyRecommendations"), argThat(map -> {
            Map<String, String> toyRecs = (Map<String, String>) map;
            return toyRecs.size() == 2 &&
                    toyRecs.get("Fluffy").equals("Dog toy") &&
                    toyRecs.get("Whiskers").equals("Cat toy");
        }));

        assertEquals("foodandtoys", viewName);
    }
}
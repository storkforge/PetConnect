package se.storkforge.petconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.storkforge.petconnect.controller.PetController;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.service.PetService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PetControllerApplicationTest { // Or a more specific name

    private MockMvc mockMvc;

    @Mock
    private PetService petService;

    @InjectMocks
    private PetController petController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Pet testPet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(petController).build();
        testPet = new Pet("Buddy", "Dog", true, 3, "John", "New York");
    }

    @Test
    public void testGetAllPetsEndpoint() throws Exception {
        // Given
        List<Pet> pets = Arrays.asList(
                testPet,
                new Pet("Misty", "Cat", false, 2, "Sarah", "Boston")
        );
        when(petService.getAllPets()).thenReturn(pets);

        // When & Then
        mockMvc.perform(get("/pets"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Buddy"))
                .andExpect(jsonPath("$[1].species").value("Cat"));

        verify(petService, times(1)).getAllPets();
    }

    // Add more test methods for other controller endpoints
}
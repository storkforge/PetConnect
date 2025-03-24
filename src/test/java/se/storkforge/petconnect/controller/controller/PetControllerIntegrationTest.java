package se.storkforge.petconnect.controller.controller;

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

public class PetControllerIntegrationTest {

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

    @Test
    public void testGetPetByIdEndpoint() throws Exception {
        // Given
        when(petService.getPetById(1L)).thenReturn(Optional.of(testPet));

        // When & Then
        mockMvc.perform(get("/pets/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Buddy"));

        verify(petService, times(1)).getPetById(1L);
    }

    @Test
    public void testGetPetByIdNotFoundEndpoint() throws Exception {
        // Given
        when(petService.getPetById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/pets/1"))
                .andExpect(status().isNotFound());

        verify(petService, times(1)).getPetById(1L);
    }

    @Test
    public void testCreatePetEndpoint() throws Exception {
        // Given
        String petJson = objectMapper.writeValueAsString(testPet);
        when(petService.createPet(any(Pet.class))).thenReturn(testPet);

        // When & Then
        mockMvc.perform(post("/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(petJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Buddy"));

        verify(petService, times(1)).createPet(any(Pet.class));
    }

    @Test
    public void testUpdatePetEndpoint() throws Exception {
        // Given
        String petJson = objectMapper.writeValueAsString(testPet);
        when(petService.updatePet(eq(1L), any(Pet.class))).thenReturn(testPet);

        // When & Then
        mockMvc.perform(put("/pets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(petJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Buddy"));

        verify(petService, times(1)).updatePet(eq(1L), any(Pet.class));
    }

    @Test
    public void testUpdatePetNotFoundEndpoint() throws Exception {
        // Given
        String petJson = objectMapper.writeValueAsString(testPet);
        when(petService.updatePet(eq(1L), any(Pet.class))).thenReturn(null);

        // When & Then
        mockMvc.perform(put("/pets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(petJson))
                .andExpect(status().isNotFound());

        verify(petService, times(1)).updatePet(eq(1L), any(Pet.class));
    }

    @Test
    public void testDeletePetEndpoint() throws Exception {
        // When & Then
        mockMvc.perform(delete("/pets/1"))
                .andExpect(status().isNoContent());

        verify(petService, times(1)).deletePet(eq(1L));
    }
}
package se.storkforge.petconnect;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.storkforge.petconnect.controller.PetController;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.exception.PetNotFoundException;
import se.storkforge.petconnect.service.PetService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class PetControllerApplicationTest {

    private MockMvc mockMvc;

    @Mock
    private PetService petService;

    @InjectMocks
    private PetController petController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Pet testPet;
    private final Long testPetId = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(petController).build();
        testPet = new Pet("Buddy", "Dog", true, 3, "John", "New York");
        testPet.setId(testPetId); // Set an ID for the test pet
    }

    @Test
    void testGetAllPetsEndpoint() throws Exception {
        // Given
        List<Pet> pets = Arrays.asList(
                testPet,
                new Pet("Misty", "Cat", false, 2, "Sarah", "Boston")
        );
        Pageable pageable = PageRequest.of(0, 10); // Använd PageRequest
        Page<Pet> petPage = new PageImpl<>(pets, pageable, pets.size()); // Använd alla argument

        when(petService.getAllPets(any(Pageable.class))).thenReturn(petPage);

        // When & Then
        mockMvc.perform(get("/pets"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Buddy"))
                .andExpect(jsonPath("$.content[1].name").value("Misty"));

        verify(petService, times(1)).getAllPets(any(Pageable.class));
    }

    @Test
    void testGetPetByIdEndpointFound() throws Exception {
        // Given
        when(petService.getPetById(testPetId)).thenReturn(Optional.of(testPet));

        // When & Then
        mockMvc.perform(get("/pets/{id}", testPetId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Buddy"))
                .andExpect(jsonPath("$.species").value("Dog"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.age").value(3))
                .andExpect(jsonPath("$.owner").value("John"))
                .andExpect(jsonPath("$.location").value("New York"));

        verify(petService, times(1)).getPetById(testPetId);
    }

    @Test
    void testCreatePetEndpoint() throws Exception {
        // Given
        Pet newPet = new Pet("Lucy", "Rabbit", false, 1, "Alice", "London");
        when(petService.createPet(any(Pet.class))).thenReturn(newPet);
        String petJson = objectMapper.writeValueAsString(newPet);

        // When & Then
        mockMvc.perform(post("/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(petJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Lucy"))
                .andExpect(jsonPath("$.species").value("Rabbit"))
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.age").value(1))
                .andExpect(jsonPath("$.owner").value("Alice"))
                .andExpect(jsonPath("$.location").value("London"));

        verify(petService, times(1)).createPet(any(Pet.class));
    }

    @Test
    void testUpdatePetEndpointFound() throws Exception {
        // Given
        Pet updatedPet = new Pet("Buddy Updated", "Dog", true, 4, "John Doe", "Somewhere");
        updatedPet.setId(testPetId);
        when(petService.updatePet(eq(testPetId), any(Pet.class))).thenReturn(updatedPet);
        String petJson = objectMapper.writeValueAsString(updatedPet);

        // When & Then
        mockMvc.perform(put("/pets/{id}", testPetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(petJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testPetId))
                .andExpect(jsonPath("$.name").value("Buddy Updated"))
                .andExpect(jsonPath("$.owner").value("John Doe"))
                .andExpect(jsonPath("$.species").value("Dog"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.age").value(4))
                .andExpect(jsonPath("$.location").value("Somewhere"));

        verify(petService, times(1)).updatePet(eq(testPetId), any(Pet.class));
    }

    @Test
    void testUpdatePetEndpointNotFound() throws Exception {
        // Given
        Long notFoundId = 999L;
        Pet updatedPet = new Pet("Updated", "Cat", true, 5, "Someone", "Here");
        when(petService.updatePet(eq(notFoundId), any(Pet.class)))
                .thenThrow(new PetNotFoundException("Pet with id " + notFoundId + " not found"));
        String petJson = objectMapper.writeValueAsString(updatedPet);

        // When & Then
        mockMvc.perform(put("/pets/{id}", notFoundId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(petJson))
                .andExpect(status().isNotFound());

        verify(petService, times(1)).updatePet(eq(notFoundId), any(Pet.class));
    }
    @Test
    void testDeletePetEndpointFound() throws Exception {
        // Given
        doNothing().when(petService).deletePet(eq(testPetId));

        // When & Then
        mockMvc.perform(delete("/pets/{id}", testPetId))
                .andExpect(status().isNoContent());

        verify(petService, times(1)).deletePet(eq(testPetId));
    }

    @Test
    void testDeletePetEndpointNotFound() throws Exception {
        // Given
        Long notFoundId = 999L;
        doThrow(new PetNotFoundException("Pet with id " + notFoundId + " not found"))
                .when(petService).deletePet(eq(notFoundId)); // Simulate PetNotFoundException

        // When & Then
        ResultActions resultActions = mockMvc.perform(delete("/pets/{id}", notFoundId));

        resultActions.andExpect(status().isNotFound())
                .andExpect(content().string("")); // Expect an empty response body

        verify(petService, times(1)).deletePet(eq(notFoundId));
    }
}
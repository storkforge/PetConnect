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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.storkforge.petconnect.controller.PetController;
import se.storkforge.petconnect.dto.PetRequestDTO;
import se.storkforge.petconnect.dto.PetResponseDTO;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.exception.PetNotFoundException;
import se.storkforge.petconnect.service.PetService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    private PetResponseDTO testPetResponseDTO;
    private final Long testPetId = 1L;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(petController).build();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("john_user");
        testUser.setEmail("john@example.com");
        testUser.setPassword("password");

        testPet = new Pet("Buddy", "Dog", true, 3, testUser, "New York");
        testPet.setId(testPetId);

        new PetRequestDTO(
                "Buddy", "Dog", true, 3, "john_user", "New York");

        testPetResponseDTO = new PetResponseDTO(
                testPetId, "Buddy", "Dog", true, 3, "john_user", "New York", null);
    }

    @Test
    void testGetAllPetsEndpoint() throws Exception {
        // Setup test data
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("sarah_user");

        Pet pet2 = new Pet("Misty", "Cat", false, 2, user2, "Boston");
        pet2.setId(2L);

        List<Pet> pets = Arrays.asList(testPet, pet2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pet> petPage = new PageImpl<>(pets, pageable, pets.size());

        PetResponseDTO responseDTO2 = new PetResponseDTO(
                2L, "Misty", "Cat", false, 2, "sarah_user", "Boston", null);

        // Mock service calls
        when(petService.getAllPets(pageable)).thenReturn(petPage);
        when(petService.convertToPetResponseDTO(testPet)).thenReturn(testPetResponseDTO);
        when(petService.convertToPetResponseDTO(pet2)).thenReturn(responseDTO2);

        // Execute and verify
        mockMvc.perform(get("/pets"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Buddy"))
                .andExpect(jsonPath("$.content[1].name").value("Misty"));
    }

    @Test
    void testGetPetByIdEndpointFound() throws Exception {
        when(petService.getPetById(testPetId)).thenReturn(Optional.of(testPet));
        when(petService.convertToPetResponseDTO(testPet)).thenReturn(testPetResponseDTO);

        mockMvc.perform(get("/pets/{id}", testPetId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Buddy"))
                .andExpect(jsonPath("$.owner").value("john_user"));
    }

    @Test
    void testCreatePetEndpoint() throws Exception {
        PetRequestDTO newPetRequest = new PetRequestDTO(
                "Lucy", "Rabbit", false, 1, "alice_user", "London");

        Pet newPet = new Pet("Lucy", "Rabbit", false, 1, testUser, "London");
        PetResponseDTO newPetResponse = new PetResponseDTO(
                null, "Lucy", "Rabbit", false, 1, "alice_user", "London", null);

        when(petService.convertToPet(newPetRequest)).thenReturn(newPet);
        when(petService.createPet(newPet)).thenReturn(newPet);
        when(petService.convertToPetResponseDTO(newPet)).thenReturn(newPetResponse);

        mockMvc.perform(post("/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPetRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Lucy"))
                .andExpect(jsonPath("$.owner").value("alice_user"));
    }

    @Test
    void testUpdatePetEndpointFound() throws Exception {
        PetRequestDTO updateRequest = new PetRequestDTO(
                "Buddy Updated", "Dog", true, 4, "johndoe_user", "Somewhere");

        Pet updatedPet = new Pet("Buddy Updated", "Dog", true, 4, testUser, "Somewhere");
        updatedPet.setId(testPetId);
        PetResponseDTO updatedResponse = new PetResponseDTO(
                testPetId, "Buddy Updated", "Dog", true, 4, "johndoe_user", "Somewhere", null);

        when(petService.convertToPet(updateRequest)).thenReturn(updatedPet);
        when(petService.updatePet(testPetId, updatedPet)).thenReturn(updatedPet);
        when(petService.convertToPetResponseDTO(updatedPet)).thenReturn(updatedResponse);

        mockMvc.perform(put("/pets/{id}", testPetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Buddy Updated"))
                .andExpect(jsonPath("$.owner").value("johndoe_user"));
    }

    @Test
    void testUpdatePetEndpointNotFound() throws Exception {
        Long notFoundId = 999L;
        PetRequestDTO updateRequest = new PetRequestDTO(
                "Updated", "Cat", true, 5, "someone_user", "Here");

        when(petService.convertToPet(updateRequest)).thenReturn(new Pet());
        when(petService.updatePet(eq(notFoundId), any(Pet.class)))
                .thenThrow(new PetNotFoundException("Pet not found"));

        mockMvc.perform(put("/pets/{id}", notFoundId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeletePetEndpointFound() throws Exception {
        doNothing().when(petService).deletePet(testPetId);

        mockMvc.perform(delete("/pets/{id}", testPetId))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeletePetEndpointNotFound() throws Exception {
        Long notFoundId = 999L;
        doThrow(new PetNotFoundException("Pet not found"))
                .when(petService).deletePet(notFoundId);

        mockMvc.perform(delete("/pets/{id}", notFoundId))
                .andExpect(status().isNotFound());
    }
}
package se.storkforge.petconnect.controller;

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
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.storkforge.petconnect.dto.PetInputDTO;
import se.storkforge.petconnect.dto.PetUpdateInputDTO;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.service.PetService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class PetControllerIntegrationTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private PetService petService;
    @Mock private Authentication authentication;
    @InjectMocks private PetController petController;

    private Pet testPet;
    private User testUser;
    private final Long testPetId = 1L;
    private final String testUsername = "testUser";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(petController).build();

        testUser = new User(testUsername, "test@example.com", "password");
        testUser.setId(1L);

        testPet = new Pet();
        testPet.setId(testPetId);
        testPet.setName("Buddy");
        testPet.setSpecies("Dog");
        testPet.setAvailable(true);
        testPet.setAge(3);
        testPet.setOwner(testUser);
        testPet.setLocation("New York");
    }

    @Test
    void testGetAllPets() throws Exception {
        // Create a proper Page implementation
        List<Pet> pets = List.of(testPet);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pet> petPage = new PageImpl<>(pets, pageable, pets.size());

        // Mock the service call
        when(petService.getAllPets(any(Pageable.class))).thenReturn(petPage);

        // Perform the request and verify
        mockMvc.perform(get("/pets")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].name").value("Buddy"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void testGetPetById() throws Exception {
        when(petService.getPetById(testPetId)).thenReturn(Optional.of(testPet));

        mockMvc.perform(get("/pets/{id}", testPetId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Buddy"));
    }

    @Test
    void testCreatePet() throws Exception {
        PetInputDTO inputDTO = new PetInputDTO(
                "Buddy", "Dog", true, 3, testUser.getId(), "New York");

        when(petService.createPet(any(PetInputDTO.class))).thenReturn(testPet);

        mockMvc.perform(post("/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Buddy"));
    }

    @Test
    void testUpdatePet() throws Exception {
        when(authentication.getName()).thenReturn(testUsername);

        PetUpdateInputDTO updateDTO = new PetUpdateInputDTO(
                "Buddy Updated", null, null, null, null, "New Location");

        Pet updatedPet = new Pet();
        updatedPet.setId(testPetId);
        updatedPet.setName("Buddy Updated");
        updatedPet.setLocation("New Location");
        updatedPet.setOwner(testUser);

        when(petService.updatePet(eq(testPetId), any(PetUpdateInputDTO.class), eq(testUsername)))
                .thenReturn(updatedPet);

        mockMvc.perform(put("/pets/{id}", testPetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO))
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Buddy Updated"));
    }

    @Test
    void testDeletePet() throws Exception {
        when(authentication.getName()).thenReturn(testUsername);
        doNothing().when(petService).deletePet(eq(testPetId), eq(testUsername));

        mockMvc.perform(delete("/pets/{id}", testPetId)
                        .principal(authentication))
                .andExpect(status().isNoContent());
    }
}
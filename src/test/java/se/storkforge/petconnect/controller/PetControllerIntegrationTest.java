package se.storkforge.petconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import se.storkforge.petconnect.dto.PetInputDTO;
import se.storkforge.petconnect.dto.PetUpdateInputDTO;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.exception.PetNotFoundException;
import se.storkforge.petconnect.service.PetFilter;
import se.storkforge.petconnect.service.PetService;

import java.util.List;
import java.util.Optional;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertNull;
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
        mockMvc = MockMvcBuilders.standaloneSetup(petController)
                .setControllerAdvice(new ExceptionHandlerController())
                .build();

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
        List<Pet> pets = List.of(testPet);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pet> petPage = new PageImpl<>(pets, pageable, pets.size());

        ArgumentCaptor<PetFilter> filterCaptor = ArgumentCaptor.forClass(PetFilter.class);
        when(petService.getAllPets(any(Pageable.class), any(PetFilter.class)))
                .thenReturn(petPage);

        mockMvc.perform(get("/pets")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].name").value("Buddy"));
        verify(petService).getAllPets(any(Pageable.class), filterCaptor.capture());
        PetFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getSpecies());
        assertNull(capturedFilter.getAvailable());
        assertNull(capturedFilter.getMinAge());
        assertNull(capturedFilter.getMaxAge());
        assertNull(capturedFilter.getLocation());
        assertNull(capturedFilter.getNameContains());
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

        when(authentication.getName()).thenReturn(testUsername);
        when(petService.createPet(any(PetInputDTO.class), eq(testUsername))).thenReturn(testPet);

        mockMvc.perform(post("/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO))
                        .principal(authentication))
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

    @Test
    void testGetAllPetsWithFilter() throws Exception {
        List<Pet> pets = List.of(testPet);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pet> petPage = new PageImpl<>(pets, pageable, pets.size());

        when(petService.getAllPets(any(Pageable.class), any(PetFilter.class))).thenReturn(petPage);

        mockMvc.perform(get("/pets")
                        .param("page", "0")
                        .param("size", "10")
                        .param("species", "Dog")
                        .param("available", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].species").value("Dog"));
    }

    @Test
    void testGetPetById_NotFound() throws Exception {
        when(petService.getPetById(testPetId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/pets/{id}", testPetId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreatePet_InvalidInput() throws Exception {
        PetInputDTO invalidInput = new PetInputDTO(
                null, "Dog", true, 3, testUser.getId(), "New York");

        mockMvc.perform(post("/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdatePet_Unauthorized() throws Exception {
        when(authentication.getName()).thenReturn("differentUser");

        PetUpdateInputDTO updateDTO = new PetUpdateInputDTO(null, null, null, null, null, null);

        when(petService.updatePet(eq(testPetId), any(PetUpdateInputDTO.class), eq("differentUser")))
                .thenThrow(new SecurityException("Unauthorized"));

        mockMvc.perform(put("/pets/{id}", testPetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO))
                        .principal(authentication))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeletePet_NotFound() throws Exception {
        when(authentication.getName()).thenReturn(testUsername);
        doThrow(new PetNotFoundException("Not found"))
                .when(petService).deletePet(testPetId, testUsername);

        mockMvc.perform(delete("/pets/{id}", testPetId)
                        .principal(authentication))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUploadProfilePicture() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test image content".getBytes());
        doNothing().when(petService).uploadProfilePicture(eq(testPetId), any(MultipartFile.class));

        mockMvc.perform(multipart("/pets/{id}/PFP", testPetId)
                        .file(file))
                .andExpect(status().isOk());
        verify(petService).uploadProfilePicture(eq(testPetId), any(MultipartFile.class));
    }

    @Test
    void testGetProfilePicture() throws Exception {
        Resource mockResource = mock(Resource.class);
        when(mockResource.getFilename()).thenReturn("test.png");
        when(petService.getProfilePicture(testPetId)).thenReturn(mockResource);

        mockMvc.perform(get("/pets/{id}/PFP", testPetId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }
}
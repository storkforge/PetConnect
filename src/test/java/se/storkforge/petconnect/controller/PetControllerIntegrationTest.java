package se.storkforge.petconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Pet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import se.storkforge.petconnect.repository.PetRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = se.storkforge.petconnect.PetConnectApplication.class) // Explicitly specify the main application class
@AutoConfigureMockMvc
public class PetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PetRepository petRepository;

    @Test
    public void testGetAllPetsEndpoint() throws Exception {
        mockMvc.perform(get("/pets"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testCreatePetEndpoint() throws Exception {
        Pet pet = new Pet("Buddy", "Dog", true, 3, "John", "New York");
        String petJson = objectMapper.writeValueAsString(pet);

        mockMvc.perform(post("/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(petJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Buddy"));
    }

    @Test
    public void testUpdatePetEndpoint() throws Exception {
        Pet pet = new Pet("Buddy", "Dog", true, 3, "John", "New York");
        pet = petRepository.save(pet);
        Pet updatedPet = new Pet("New Buddy", "Dog", true, 4, "John", "New York");
        String updatedPetJson = objectMapper.writeValueAsString(updatedPet);

        mockMvc.perform(put("/pets/" + pet.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedPetJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Buddy"));
    }

    @Test
    public void testDeletePetEndpoint() throws Exception {
        Pet pet = new Pet("Buddy", "Dog", true, 3, "John", "New York");
        pet = petRepository.save(pet);

        mockMvc.perform(delete("/pets/" + pet.getId()))
                .andExpect(status().isNoContent());
    }
}
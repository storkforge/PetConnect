package se.storkforge.petconnect.controller;
import entity.Pet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.storkforge.petconnect.repository.PetRepository;
import se.storkforge.petconnect.service.PetService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @InjectMocks
    private PetService petService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllPets() {
        Pet pet1 = new Pet("Buddy", "Dog", true, 3, "John", "New York");
        Pet pet2 = new Pet("Whiskers", "Cat", false, 5, "Jane", "London");
        List<Pet> pets = Arrays.asList(pet1, pet2);

        when(petRepository.findAll()).thenReturn(pets);

        List<Pet> result = petService.getAllPets();

        assertEquals(2, result.size());
        assertEquals("Buddy", result.get(0).getName());
        verify(petRepository, times(1)).findAll();
    }

    @Test
    public void testGetPetById() {
        Pet pet = new Pet("Buddy", "Dog", true, 3, "John", "New York");
        pet.setId(1L);

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        Optional<Pet> result = petService.getPetById(1L);

        assertTrue(result.isPresent());
        assertEquals("Buddy", result.get().getName());
        verify(petRepository, times(1)).findById(1L);
    }

    @Test
    public void testCreatePet() {
        Pet pet = new Pet("Buddy", "Dog", true, 3, "John", "New York");

        when(petRepository.save(pet)).thenReturn(pet);

        Pet result = petService.createPet(pet);

        assertEquals("Buddy", result.getName());
        verify(petRepository, times(1)).save(pet);
    }

    @Test
    public void testUpdatePet() {
        Pet existingPet = new Pet("Buddy", "Dog", true, 3, "John", "New York");
        existingPet.setId(1L);
        Pet updatedPet = new Pet("New Buddy", "Dog", true, 4, "John", "New York");

        when(petRepository.existsById(1L)).thenReturn(true);
        when(petRepository.save(any(Pet.class))).thenReturn(updatedPet);

        Pet result = petService.updatePet(1L, updatedPet);

        assertEquals("New Buddy", result.getName());
        verify(petRepository, times(1)).existsById(1L);
        verify(petRepository, times(1)).save(any(Pet.class));
    }

    @Test
    public void testDeletePet() {
        petService.deletePet(1L);
        verify(petRepository, times(1)).deleteById(1L);
    }
}
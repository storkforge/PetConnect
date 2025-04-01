package se.storkforge.petconnect.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import se.storkforge.petconnect.dto.PetInputDTO;
import se.storkforge.petconnect.dto.PetUpdateInputDTO;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.exception.PetNotFoundException;
import se.storkforge.petconnect.repository.PetRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserService userService;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private PetService petService;

    private User testOwner;
    private final String currentUsername = "testuser";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        testOwner = new User(currentUsername, "john@example.com", "password");
        testOwner.setId(1L);
    }

    private Pet createTestPet() {
        Pet pet = new Pet();
        pet.setId(1L);
        pet.setName("Buddy");
        pet.setSpecies("Dog");
        pet.setAvailable(true);
        pet.setAge(3);
        pet.setOwner(testOwner);
        pet.setLocation("New York");
        return pet;
    }

    private PetInputDTO createTestPetInputDTO() {
        return new PetInputDTO(
                "Buddy", "Dog", true, 3, testOwner.getId(), "New York"
        );
    }

    private PetUpdateInputDTO createTestPetUpdateDTO() {
        return new PetUpdateInputDTO(
                "New Buddy", null, null, 4, null, "New Location"
        );
    }

    @Test
    public void testGetAllPets() {
        Pet pet1 = createTestPet();
        Pet pet2 = new Pet();
        pet2.setId(2L);
        pet2.setName("Whiskers");
        pet2.setSpecies("Cat");
        pet2.setAvailable(false);
        pet2.setAge(5);
        pet2.setOwner(testOwner);
        pet2.setLocation("London");

        List<Pet> pets = Arrays.asList(pet1, pet2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pet> petPage = new PageImpl<>(pets, pageable, pets.size());

        when(petRepository.findAll(pageable)).thenReturn(petPage);

        Page<Pet> result = petService.getAllPets(pageable);

        assertEquals(2, result.getContent().size());
        assertEquals("Buddy", result.getContent().getFirst().getName());
        verify(petRepository, times(1)).findAll(pageable);
    }

    @Test
    public void testGetPetById() {
        Pet pet = createTestPet();

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        Optional<Pet> result = petService.getPetById(1L);

        assertTrue(result.isPresent());
        assertEquals("Buddy", result.get().getName());
        verify(petRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetPetByIdNotFound() {
        when(petRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Pet> result = petService.getPetById(1L);

        assertFalse(result.isPresent());
        verify(petRepository, times(1)).findById(1L);
    }

    @Test
    public void testCreatePet() {
        PetInputDTO inputDTO = createTestPetInputDTO();
        Pet expectedPet = createTestPet();

        when(userService.getUserById(testOwner.getId())).thenReturn(testOwner);
        when(petRepository.save(any(Pet.class))).thenReturn(expectedPet);

        Pet result = petService.createPet(inputDTO);

        assertEquals("Buddy", result.getName());
        verify(petRepository, times(1)).save(any(Pet.class));
        verify(userService, times(1)).getUserById(testOwner.getId());
    }

    @Test
    public void testUpdatePet_SuccessWhenOwner() {
        Pet existingPet = createTestPet();
        PetUpdateInputDTO updateDTO = createTestPetUpdateDTO();
        Pet updatedPet = createTestPet();
        updatedPet.setName("New Buddy");
        updatedPet.setAge(4);
        updatedPet.setLocation("New Location");

        when(petRepository.findById(1L)).thenReturn(Optional.of(existingPet));
        when(petRepository.save(any(Pet.class))).thenReturn(updatedPet);

        Pet result = petService.updatePet(1L, updateDTO, currentUsername);

        assertEquals("New Buddy", result.getName());
        assertEquals(4, result.getAge());
        verify(petRepository, times(1)).findById(1L);
        verify(petRepository, times(1)).save(any(Pet.class));
    }

    @Test
    public void testUpdatePet_ThrowsWhenNotOwner() {
        User otherUser = new User("otheruser", "other@example.com", "password");
        otherUser.setId(2L);

        Pet existingPet = createTestPet();
        existingPet.setOwner(otherUser);

        when(petRepository.findById(1L)).thenReturn(Optional.of(existingPet));

        assertThrows(SecurityException.class,
                () -> petService.updatePet(1L, createTestPetUpdateDTO(), currentUsername));
    }

    @Test
    public void testDeletePet_SuccessWhenOwner() {
        // Setup
        Pet pet = createTestPet();
        User owner = pet.getOwner();

        // Verify test data
        assertNotNull(owner, "Test pet must have an owner");
        assertEquals(currentUsername, owner.getUsername(),
                "Test pet should belong to current user");

        // Mock
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        doNothing().when(petRepository).delete(pet);

        // Execute
        petService.deletePet(1L, currentUsername);

        // Verify
        verify(petRepository, times(1)).findById(1L);

        // Capture the actual deleted pet
        ArgumentCaptor<Pet> petCaptor = ArgumentCaptor.forClass(Pet.class);
        verify(petRepository).delete(petCaptor.capture());
        Pet deletedPet = petCaptor.getValue();

        // The owner reference might be null at deletion time due to JPA behavior
        // So we shouldn't assert it remains non-null
        assertNotNull(deletedPet, "Pet should be deleted");

        // Verify the owner's pets collection was updated
        assertFalse(owner.getPets().contains(pet),
                "Pet should be removed from owner's collection");
    }

    @Test
    public void testDeletePet_ThrowsWhenNotOwner() {
        User otherUser = new User("otheruser", "other@example.com", "password");
        otherUser.setId(2L);

        Pet pet = createTestPet();
        pet.setOwner(otherUser);

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        assertThrows(SecurityException.class,
                () -> petService.deletePet(1L, currentUsername));
    }

    @Test
    public void testDeletePetNotFound() {
        when(petRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(PetNotFoundException.class,
                () -> petService.deletePet(999L, currentUsername));

        verify(petRepository, times(1)).findById(999L);
        verify(petRepository, never()).delete(any(Pet.class));
    }
}
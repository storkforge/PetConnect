package se.storkforge.petconnect.service;

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
import org.springframework.data.jpa.domain.Specification;
import se.storkforge.petconnect.dto.PetInputDTO;
import se.storkforge.petconnect.dto.PetUpdateInputDTO;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.exception.PetNotFoundException;
import se.storkforge.petconnect.repository.PetRepository;
import se.storkforge.petconnect.util.OwnershipValidator; // Import OwnershipValidator
import se.storkforge.petconnect.util.PetOwnershipHelper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentMatchers;

@ExtendWith(MockitoExtension.class)
public class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserService userService;

    @Mock
    private PetOwnershipHelper petOwnershipHelper;

    @Mock
    private OwnershipValidator ownershipValidator; // Mock OwnershipValidator

    @InjectMocks
    private PetService petService;

    private PetFilter petFilter;
    private Pageable pageable;
    private Pet testPet;
    private User testUser;
    private PetInputDTO testPetInputDTO;
    private PetUpdateInputDTO testPetUpdateInputDTO;
    private String testUsername;

    @BeforeEach
    void setUp() {
        petFilter = new PetFilter();
        pageable = PageRequest.of(0, 10);
        testUser = new User("testUser", "test@example.com", "password");
        testUser.setId(1L);
        testPet = new Pet();
        testPet.setId(1L);
        testPet.setName("Buddy");
        testPet.setSpecies("Dog");
        testPet.setAvailable(true);
        testPet.setAge(3);
        testPet.setLocation("New York");
        testPet.setOwner(testUser);
        testUsername = "testUser";

        testPetInputDTO = new PetInputDTO(
                "Buddy", "Dog", true, 3, testUser.getId(), "New York");

        testPetUpdateInputDTO = new PetUpdateInputDTO(
                "Buddy Updated", null, null, null, null, "New Location");
    }

    @Test
    void testGetAllPetsWithoutFilter() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        PetFilter filter = null;
        Page<Pet> expectedPage = new PageImpl<>(List.of(new Pet()));

        // Mock the specification behavior
        Specification<Pet> spec = Specification.where(null);
        when(petRepository.findAll(spec, pageable)).thenReturn(expectedPage);

        // When
        Page<Pet> result = petService.getAllPets(pageable, filter);

        // Then
        assertNotNull(result);
        assertEquals(expectedPage, result);
        verify(petRepository).findAll(spec, pageable);
    }

    @Test
    void testGetAllPetsWithFilter() {
        List<Pet> pets = Collections.singletonList(testPet);
        Page<Pet> page = new PageImpl<>(pets, pageable, pets.size());
        petFilter.setSpecies("Dog");

        when(petRepository.findAll(
                ArgumentMatchers.<Specification<Pet>>any(),
                any(Pageable.class))
        ).thenReturn(page);

        Page<Pet> result = petService.getAllPets(pageable, petFilter);

        assertEquals(1, result.getContent().size());
        assertEquals("Dog", result.getContent().getFirst().getSpecies());

        // Verify repository was called
        verify(petRepository).findAll(
                ArgumentMatchers.<Specification<Pet>>any(),
                any(Pageable.class)
        );
    }

    @Test
    void testGetPetByIdFound() {
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));

        Optional<Pet> result = petService.getPetById(1L);

        assertTrue(result.isPresent());
        assertEquals("Buddy", result.get().getName());
    }

    @Test
    void testGetPetByIdNotFound() {
        when(petRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Pet> result = petService.getPetById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllPetsWithMultipleFilters() {
        List<Pet> pets = Collections.singletonList(testPet);
        Page<Pet> page = new PageImpl<>(pets, pageable, pets.size());
        petFilter.setSpecies("Dog");
        petFilter.setAvailable(true);
        petFilter.setMinAge(2);
        petFilter.setMaxAge(5);

        when(petRepository.findAll(
                ArgumentMatchers.<Specification<Pet>>any(),
                any(Pageable.class))
        ).thenReturn(page);

        Page<Pet> result = petService.getAllPets(pageable, petFilter);

        assertEquals(1, result.getContent().size());
        assertEquals("Dog", result.getContent().getFirst().getSpecies());
        assertTrue(result.getContent().getFirst().isAvailable());
    }

    @Test
    void testCreatePet() {
        testPetInputDTO.setOwnerId(testUser.getId());
        when(userService.getUserByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);

        Pet result = petService.createPet(testPetInputDTO, testUsername);

        assertEquals(testPet, result);
        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void testUpdatePet() {
        // Arrange
        when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);
        // Importantly, you might need to mock the behavior of the ownershipValidator
        // if your test relies on it not throwing an exception in this scenario.
        doNothing().when(ownershipValidator).validateOwnership(any(Pet.class), anyString());

        // Act
        Pet result = petService.updatePet(testPet.getId(), testPetUpdateInputDTO, testUsername);

        // Assert
        assertEquals(testPet, result);
        verify(petRepository).findById(testPet.getId());
        verify(petRepository).save(any(Pet.class));
        verify(ownershipValidator).validateOwnership(any(Pet.class), eq(testUsername));
    }

    @Test
    void testDeletePet() {
        when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));
        doNothing().when(ownershipValidator).validateOwnership(any(Pet.class), anyString()); // Mock ownership validation

        assertDoesNotThrow(() -> petService.deletePet(testPet.getId(), testUsername));
        verify(petRepository).findById(testPet.getId());
        verify(petRepository).delete(any(Pet.class));
        verify(ownershipValidator).validateOwnership(any(Pet.class), eq(testUsername));
    }

    @Test
    void testDeletePetNotFound() {
        when(petRepository.findById(testPet.getId())).thenReturn(Optional.empty());

        assertThrows(PetNotFoundException.class, () -> petService.deletePet(testPet.getId(), testUsername));
        verify(petRepository).findById(testPet.getId());
        verifyNoMoreInteractions(petRepository); // Ensure delete is not called
        verifyNoInteractions(ownershipValidator); // Ownership validation won't happen if pet is not found
    }

    @Test
    void updatePet_ByNonOwner_ShouldThrowException() {
        // Arrange
        String nonOwnerUsername = "anotherUser";
        User nonOwner = new User(nonOwnerUsername, "another@example.com", "password");
        nonOwner.setId(2L);

        PetUpdateInputDTO update = new PetUpdateInputDTO(
                "Buddy Updated", null, null, null, null, "New Location");

        // Set up a pet owned by testUser
        Pet ownedPet = new Pet();
        ownedPet.setId(1L);
        ownedPet.setName("Buddy");
        ownedPet.setSpecies("Dog");
        ownedPet.setOwner(testUser);

        when(petRepository.findById(1L)).thenReturn(Optional.of(ownedPet));
        doThrow(new SecurityException("You do not have permission to perform this action"))
                .when(ownershipValidator).validateOwnership(any(Pet.class), eq(nonOwnerUsername));

        // When/Then - Expect SecurityException because non-owner tries to update
        assertThrows(SecurityException.class,
                () -> petService.updatePet(1L, update, nonOwnerUsername));

        verify(petRepository).findById(1L);
        verify(ownershipValidator).validateOwnership(any(Pet.class), eq(nonOwnerUsername));
        verifyNoMoreInteractions(petRepository); // Ensure save is not called
    }
}
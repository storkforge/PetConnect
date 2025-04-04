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

        // Verify repository was called
        verify(petRepository).findAll(
                ArgumentMatchers.<Specification<Pet>>any(),
                any(Pageable.class)
        );
    }

    @Test
    void testPetFilterValidation() {
        // Given
        PetFilter filter = new PetFilter();
        filter.setMinAge(5);
        filter.setMaxAge(3);

        // When/Then
        assertThrows(IllegalArgumentException.class, filter::validate);

        // Given valid values
        filter.setMinAge(2);
        filter.setMaxAge(5);

        // When/Then (should not throw)
        assertDoesNotThrow(filter::validate);
    }

    @Test
    void testCreatePet() {
        when(userService.getUserById(testUser.getId())).thenReturn(testUser); // Mock getUserById
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);

        System.out.println("Owner ID from DTO: " + testPetInputDTO.ownerId());

        Pet result = petService.createPet(testPetInputDTO, testUsername);

        assertEquals(testPet, result);
        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void testUpdatePet() {
        when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);

        Pet result = petService.updatePet(testPet.getId(), testPetUpdateInputDTO, testUsername);

        assertEquals(testPet, result);
        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void testDeletePet() {
        when(petRepository.findById(testPet.getId())).thenReturn(Optional.of(testPet));

        petService.deletePet(testPet.getId(), testUsername);

        verify(petRepository).delete(testPet);
    }

    @Test
    void testDeletePetNotFound() {
        when(petRepository.findById(testPet.getId())).thenReturn(Optional.empty());

        assertThrows(PetNotFoundException.class, () -> petService.deletePet(testPet.getId(), testUsername));
    }
}
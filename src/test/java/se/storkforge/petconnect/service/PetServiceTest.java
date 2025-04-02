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
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.repository.PetRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentMatchers;

@ExtendWith(MockitoExtension.class)
public class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @InjectMocks
    private PetService petService;

    private PetFilter petFilter;
    private Pageable pageable;
    private Pet testPet;

    @BeforeEach
    void setUp() {
        petFilter = new PetFilter();
        pageable = PageRequest.of(0, 10);
        testPet = new Pet();
        testPet.setId(1L);
        testPet.setName("Buddy");
        testPet.setSpecies("Dog");
        testPet.setAvailable(true);
        testPet.setAge(3);
        testPet.setLocation("New York");
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

}
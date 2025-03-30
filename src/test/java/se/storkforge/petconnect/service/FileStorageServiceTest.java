package se.storkforge.petconnect.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import se.storkforge.petconnect.entity.Pet;
import se.storkforge.petconnect.repository.PetRepository;
import se.storkforge.petconnect.service.FileStorageService;
import se.storkforge.petconnect.service.PetService;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    private Pet pet =  new Pet("Buddy",
            "Dog",
            true,
            3,
            "John",
            "New York");

    private Optional<Pet> optionalPet;
    private MockMultipartFile file;

    private FileStorageService fileStorageService;
    private PetService petService;

    private Path testDir = Path.of("uploads");

    @Mock
    private PetRepository petRepository;

    @BeforeEach
    void setUp() {
        this.optionalPet = Optional.of(pet);
        this.file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        this.fileStorageService = new FileStorageService();
        this.petService = new PetService(petRepository, fileStorageService);
        ReflectionTestUtils.setField(fileStorageService, "maxFileSize", 100L);
        ReflectionTestUtils.setField(fileStorageService, "allowedTypes", List.of("image/jpeg"));
        ReflectionTestUtils.setField(fileStorageService, "root", testDir);
    }

    @Test
    void petProfilePicturePathNotNull() throws Exception {
        when(petRepository.findById(1L)).thenReturn(optionalPet);
        petService.uploadProfilePicture(1L, file);
        assertThat(pet.getProfilePicturePath()).isNotNull();
        fileStorageService.delete(pet.getProfilePicturePath());
    }

    @Test
    void uploadFileEvenIfOldFileIsGone() throws Exception {
        when(petRepository.findById(1L)).thenReturn(optionalPet);
        petService.uploadProfilePicture(1L, file);
        fileStorageService.delete(pet.getProfilePicturePath());
        petService.uploadProfilePicture(1L, file);
        assertThat(pet.getProfilePicturePath()).isNotNull();
        fileStorageService.delete(pet.getProfilePicturePath());

    }

    @Test
    void returnFile() throws Exception {
        when(petRepository.findById(1L)).thenReturn(optionalPet);
        petService.uploadProfilePicture(1L, file);
        Resource pfp = petService.getProfilePicture(1L);
        assertThat(pfp.getURL()).isNotNull();
    }
}
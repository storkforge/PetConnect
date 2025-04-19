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
import se.storkforge.petconnect.entity.User;
import se.storkforge.petconnect.repository.PetRepository;
import se.storkforge.petconnect.service.storageService.FileStorageService;
import se.storkforge.petconnect.service.storageService.RestrictedFileStorageService;
import se.storkforge.petconnect.util.OwnershipValidator;
import se.storkforge.petconnect.util.PetOwnershipHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    private Pet pet;
    private User owner;
    private Optional<Pet> optionalPet;
    private MockMultipartFile file;
    private RestrictedFileStorageService fileStorageService;
    private PetService petService;
    private Path testDir;

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserService userService;

    @Mock
    private PetOwnershipHelper petOwnershipHelper;

    @Mock
    private OwnershipValidator ownershipValidator; // Mock OwnershipValidator

    @BeforeEach
    void setUp() throws IOException {
        // Create test owner user
        owner = new User("John", "john@example.com", "password");
        owner.setId(1L);

        // Create test pet with User owner
        pet = new Pet();
        pet.setId(1L);
        pet.setName("Buddy");
        pet.setSpecies("Dog");
        pet.setAvailable(true);
        pet.setAge(3);
        pet.setOwner(owner);
        pet.setLocation("New York");

        this.testDir = Files.createTempDirectory("test-uploads");
        this.optionalPet = Optional.of(pet);
        this.file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());

        // ✅ Use RestrictedFileStorageService instead of FileStorageService
        this.fileStorageService = new RestrictedFileStorageService();

        // ✅ Manually inject @Value properties using ReflectionTestUtils
        ReflectionTestUtils.setField(fileStorageService, "maxImageSize", 100L);
        ReflectionTestUtils.setField(fileStorageService, "allowedImageTypes", List.of("image/jpeg"));
        ReflectionTestUtils.setField(fileStorageService, "maxFileSize", 100L);
        ReflectionTestUtils.setField(fileStorageService, "allowedFileTypes", List.of("image/jpeg"));
        ReflectionTestUtils.setField(fileStorageService, "root", testDir);

        // Create PetService with all mocked dependencies
        this.petService = new PetService(
                petRepository,
                fileStorageService,
                userService,
                petOwnershipHelper,
                ownershipValidator
        );

        // Ensure directory exists
        Files.createDirectories(testDir);
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
        fileStorageService.delete(pet.getProfilePicturePath());
    }
}
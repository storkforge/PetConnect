package se.storkforge.petconnect.service.storageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import se.storkforge.petconnect.entity.Pet;
import static org.assertj.core.api.Assertions.assertThat;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@ExtendWith(MockitoExtension.class)
class RestrictedFileStorageServiceTest {

    private Pet pet;
    private MockMultipartFile file;
    private RestrictedFileStorageService rfs = new RestrictedFileStorageService();

    private void initRestrictedFileStorageService() throws IOException {
        ReflectionTestUtils.setField(rfs, "maxFileSize", 100L);
        ReflectionTestUtils.setField(rfs, "allowedFileTypes", List.of("image/jpeg"));
        ReflectionTestUtils.setField(rfs, "root", Files.createTempDirectory("test-uploads") );
    }

    public void createVariables() {
        this.pet = new Pet("Buddy", "Dog", true, 3, "John", "New York");
        this.file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
    }

    @BeforeEach
    void init() throws IOException {
        initRestrictedFileStorageService();
        createVariables();
    }

    @Test
    void createUploadsDirectory() {
        Path dir = Path.of(Objects.requireNonNull(ReflectionTestUtils.getField(rfs, "root")).toString());
        assertThat(Files.exists(dir)).isTrue();
    }

    @Test
    void successfulUpload() {
        Path path = Path.of(rfs.store(file,"test"));
        assertThat(Files.exists(path)).isTrue();
    }




}
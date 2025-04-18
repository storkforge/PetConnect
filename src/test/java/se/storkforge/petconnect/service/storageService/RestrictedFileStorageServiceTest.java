package se.storkforge.petconnect.service.storageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RestrictedFileStorageServiceTest {

    private MockMultipartFile file;
    private final RestrictedFileStorageService rfs = new RestrictedFileStorageService();

    private void initRestrictedFileStorageService() throws IOException {
        ReflectionTestUtils.setField(rfs, "maxFileSize", 100L);
        ReflectionTestUtils.setField(rfs, "allowedFileTypes", List.of("image/jpeg"));
        ReflectionTestUtils.setField(rfs, "root", Files.createTempDirectory("test-uploads") );
    }

    public void createVariables() {
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

    @Test
    void overMaxSize(){
        ReflectionTestUtils.setField(rfs, "maxFileSize", 1L);
        assertThatRuntimeException().isThrownBy(() -> rfs.storeFile(file,"test"));
    }

    @Test
    void notAllowedFileTypes(){
        ReflectionTestUtils.setField(rfs, "allowedFileTypes", List.of(""));
        assertThatRuntimeException().isThrownBy(() -> rfs.storeFile(file,"test"));
    }

    @Test
    void successfulDelete(){
        String path = rfs.storeFile(file,"test");
        rfs.delete(path);
        assertThat(Files.exists(Path.of(path))).isFalse();
    }

    @Test
    void successfulReturn() throws IOException {
        String path = rfs.storeFile(file,"test");
        assertThat(rfs.loadFile(path).isReadable()).isTrue();
    }
}
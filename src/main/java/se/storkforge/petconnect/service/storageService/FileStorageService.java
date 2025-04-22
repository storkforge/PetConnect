package se.storkforge.petconnect.service.storageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    static final Logger LOG = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir}")
    private String uploadDir;
    private Path root;

    @PostConstruct // Runs after properties are set
    private void init() {
        this.root = Paths.get(uploadDir);
        createDir(root);
    }

    String store(MultipartFile file, String dir) {
        createDir(root.resolve(dir));
        try {
            String contentType = file.getContentType();
            assert contentType != null;
            String filename = UUID.randomUUID() + "." + contentType.split("/")[1];

            Path tempDestination = root.resolve(dir);
            Path destination = tempDestination.resolve(filename);

            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination);
            }

            LOG.info("Stored file: {}", destination);
            return destination.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    public void delete(String path) {
        if (path == null) {
            LOG.warn("Attempted to delete a null filename");
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(path));
            LOG.info("Deleted file: {}", path);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file: " + path, e);
        }
    }

    public Resource loadFile(String filename) {
        try {
            Path file = Path.of(filename);

            if (!file.startsWith(root)) {
                throw new RuntimeException("Cannot access files outside upload directory");
            }

            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                System.out.println("Looking for file at: " + file);
                throw new RuntimeException("File not found or not readable: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }

    private void createDir(Path root) {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create directory", e);
        }
    }
}
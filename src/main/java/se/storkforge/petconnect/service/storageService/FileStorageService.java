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
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {
    static final Logger LOG = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir}")
    private String uploadDir;
    private Path root;

    @PostConstruct //Runs after properties
    private void init() {
        this.root = Paths.get(uploadDir);
        creatDir(root);
    }

    String store(MultipartFile file, String dir) {
        creatDir(root.resolve(dir));

        try {

            String contentType = file.getContentType();
            String filename = generateFileName(contentType); //Generate unique filename

            Path tempDestination = root.resolve(dir);
            Path destination = tempDestination.resolve(filename).normalize().toAbsolutePath();
            if (!destination.startsWith(this.root.toAbsolutePath())) {
                 throw new RuntimeException("Invalid path outside upload directory.");
            }
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination);
            }
            LOG.info("Stored file: {}", destination);
            return destination.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    private String generateFileName(String contentType) {
        String filename = UUID.randomUUID() + "." + contentType.split("/")[1];

        //if name is already taken
        Path destination = this.root.resolve(Paths.get(filename)).normalize().toAbsolutePath();
        if (Files.exists(destination)) {
            filename = generateFileName(contentType);
        }

        return filename;
    }


    void delete(String filename) {
        if (filename == null) {
            LOG.warn("Attempted to delete a null filename");
            return;
        }
        try {
            Path file = root.resolve(filename);
            Files.deleteIfExists(file);
            LOG.info("Deleted file: {}", filename);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file: " + filename, e);
        }
    }

    //Returns a Resource representing the file
    public Resource loadFile(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }

    private void creatDir(Path root) {
        System.out.println(root.toString());
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create directory", e);
        }
    }


}
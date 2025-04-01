package se.storkforge.petconnect.service;

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

    private Path root;

    //all these values are edited in the application.properties
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.max-size}")
    private long maxFileSize;

    @Value("${file.allowed-types}")
    private List<String> allowedTypes;

    @PostConstruct //Runs after properties
    private void init() {
        this.root = Paths.get(uploadDir);
        try{
            Files.createDirectories(root);  // Creates the upload directory if it doesn't exist
        } catch (IOException e) {
            throw new RuntimeException("Unable to create directory", e);
        }

    }

    public String store(MultipartFile file) {
        try {
            //Validation
            if (file.isEmpty()) {
                throw new RuntimeException("Unable to store empty file.");
            }

            if (file.getSize() > maxFileSize) {
                throw new RuntimeException("File size exceeds the allowed limit.");
            }

            String contentType = file.getContentType();
            if (contentType == null || !allowedTypes.contains(contentType)) {
                throw new RuntimeException("File type not allowed.");
            }

            //Generate unique filename
            String filename = generateFileName(contentType);

            //Save file
            Path destination = this.root.resolve(Paths.get(filename)).normalize().toAbsolutePath();
            if (!destination.startsWith(this.root.toAbsolutePath())) {
                 throw new RuntimeException("Invalid path outside upload directory.");
            }
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination);
            }
            LOG.info("Stored file: {}", destination);
            return filename;
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


    public void delete(String filename) {
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
}
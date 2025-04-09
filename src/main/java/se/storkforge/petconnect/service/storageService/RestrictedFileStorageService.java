package se.storkforge.petconnect.service.storageService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class RestrictedFileStorageService extends FileStorageService{

    //all these values are edited in the application.properties
    @Value("${image.max-size}")
    private long maxImageSize;

    @Value("${image.allowed-types}")
    private List<String> allowedImageTypes;

    @Value("${file.max-size}")
    private long maxFileSize;

    @Value("${file.allowed-types}")
    private List<String> allowedFileTypes;

    public String storeImage(MultipartFile file, String dir){
        if (isValidFile(file, maxImageSize, allowedImageTypes))
            return super.store(file, dir);
        return null;
    }

    public String storeFile(MultipartFile file, String dir){
        if (isValidFile(file, maxFileSize, allowedFileTypes))
            return super.store(file, dir);
        return null;
    }

    @Override
    public void delete(String filename){
        super.delete(filename);
    }

    public boolean isValidFile(MultipartFile file, Long maxFileSize, List<String> allowedTypes) {
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

        return true;
    }
}

package se.storkforge.petconnect.service.storageService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ImageStorageService extends FileStorageService{
    @Value("${image.max-size}")
    private long maxFileSize;

    @Value("${image.allowed-types}")
    private List<String> allowedTypes;

    public String store(MultipartFile file, String dir){
       return super.store(file, dir, maxFileSize, allowedTypes);
    }
}

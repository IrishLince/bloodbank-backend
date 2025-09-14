package RedSource.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileStorageService {

    private final CloudinaryService cloudinaryService;

    @Autowired
    public FileStorageService(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    public String storeUserPhoto(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            System.out.println("No file provided for upload");
            return null;
        }
        
        String originalFilename = file.getOriginalFilename();
        System.out.println("Processing file upload: " + originalFilename);
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            String errorMsg = "Invalid file type. Only images are allowed. Content type: " + contentType;
            System.out.println(errorMsg);
            throw new IOException(errorMsg);
        }
        
        long fileSize = file.getSize();
        System.out.println("File size: " + fileSize + " bytes");
        
        if (fileSize > 5 * 1024 * 1024) { // 5MB
            String errorMsg = "File too large. Size: " + fileSize + " bytes, Max: 5MB";
            System.out.println(errorMsg);
            throw new IOException(errorMsg);
        }

        try {
            System.out.println("Uploading file to Cloudinary...");
            String cloudinaryUrl = cloudinaryService.uploadFile(file, "user-photos");
            System.out.println("Successfully uploaded file to Cloudinary. URL: " + cloudinaryUrl);
            return cloudinaryUrl;
        } catch (IOException e) {
            String errorMsg = "Failed to upload file to cloud storage: " + e.getMessage();
            System.out.println(errorMsg);
            e.printStackTrace();
            throw new IOException(errorMsg, e);
        }
    }

    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl != null && !fileUrl.isEmpty() && fileUrl.startsWith("http")) {
            try {
                cloudinaryService.deleteFile(fileUrl);
            } catch (IOException e) {
                throw new IOException("Failed to delete file from cloud storage", e);
            }
        }
    }
}

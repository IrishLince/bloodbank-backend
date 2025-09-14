package RedSource.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        try {
            System.out.println("Starting file upload to Cloudinary...");
            System.out.println("Original filename: " + file.getOriginalFilename());
            System.out.println("Content type: " + file.getContentType());
            System.out.println("File size: " + file.getSize() + " bytes");
            
            // Generate a unique public ID for the file
            String publicId = "bloodbank/" + folder + "/" + UUID.randomUUID().toString();
            System.out.println("Generated public ID: " + publicId);
            
            // Get file bytes
            byte[] fileBytes = file.getBytes();
            System.out.println("Read " + fileBytes.length + " bytes from file");
            
            // Upload the file to Cloudinary
            System.out.println("Sending upload request to Cloudinary...");
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                fileBytes, 
                ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "auto"
                )
            );
            
            // Log the upload result
            System.out.println("Cloudinary upload result: " + uploadResult);
            
            // Get the secure URL
            String secureUrl = (String) uploadResult.get("secure_url");
            if (secureUrl == null) {
                throw new IOException("Cloudinary did not return a secure URL. Response: " + uploadResult);
            }
            
            System.out.println("Successfully uploaded file to: " + secureUrl);
            return secureUrl;
        } catch (IOException e) {
            System.err.println("Error uploading file to Cloudinary: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to upload file to Cloudinary: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String url) throws IOException {
        if (url == null || url.isEmpty()) {
            return;
        }
        
        try {
            // Extract public ID from the URL
            // Cloudinary URLs are in format: https://res.cloudinary.com/cloudname/image/upload/v1234567890/folder/filename.jpg
            // We need to get the part after /upload/ and before the version (v1234567890/)
            String uploadMarker = "/upload/";
            int uploadIndex = url.indexOf(uploadMarker);
            
            if (uploadIndex == -1) {
                throw new IOException("Invalid Cloudinary URL format");
            }
            
            // Get the part after /upload/
            String pathAfterUpload = url.substring(uploadIndex + uploadMarker.length());
            
            // Find the first slash after the version
            int versionEnd = pathAfterUpload.indexOf('/');
            if (versionEnd == -1) {
                throw new IOException("Invalid Cloudinary URL format - no version found");
            }
            
            // Get the public ID (everything after the version)
            String publicId = pathAfterUpload.substring(versionEnd + 1);
            
            // Remove file extension if present
            int dotIndex = publicId.lastIndexOf('.');
            if (dotIndex != -1) {
                publicId = publicId.substring(0, dotIndex);
            }
            
            // Delete the file from Cloudinary
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            
            if (!"ok".equals(result.get("result"))) {
                throw new IOException("Failed to delete file from Cloudinary: " + result.get("result"));
            }
        } catch (Exception e) {
            throw new IOException("Failed to delete file from Cloudinary: " + e.getMessage(), e);
        }
    }
}

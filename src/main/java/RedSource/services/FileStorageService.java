package RedSource.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public String storeUserPhoto(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Invalid file type. Only images are allowed.");
        }
        if (file.getSize() > 5 * 1024 * 1024) { // 5MB
            throw new IOException("File too large. Max 5MB");
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String safeExt = (ext != null && ext.length() <= 5) ? ext.toLowerCase() : "img";
        String filename = UUID.randomUUID() + "-" + Instant.now().toEpochMilli() + "." + safeExt;
        Path target = uploadPath.resolve(filename);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // Return a public URL path served by WebConfig
        return "/uploads/" + filename;
    }
}

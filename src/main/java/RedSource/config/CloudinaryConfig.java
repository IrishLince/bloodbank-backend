package RedSource.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.url}")
    private String cloudinaryUrl;

    @Bean
    public Cloudinary cloudinary() {
        if (cloudinaryUrl == null || cloudinaryUrl.trim().isEmpty()) {
            // Return a dummy Cloudinary instance if URL is not configured
            return new Cloudinary();
        }
        // This will automatically parse the CLOUDINARY_URL
        return new Cloudinary(cloudinaryUrl);
    }
}

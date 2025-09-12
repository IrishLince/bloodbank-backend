package RedSource.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "otps")
public class OTP {
    @Id
    private String id;
    
    private String phoneNumber;
    private String otpCode;
    private String email; // To link with signup process
    
    private Date createdAt;
    
    private boolean verified;
    private int attempts;
    
    public OTP(String phoneNumber, String otpCode, String email) {
        this.phoneNumber = phoneNumber;
        this.otpCode = otpCode;
        this.email = email;
        this.createdAt = new Date();
        this.verified = false;
        this.attempts = 0;
    }
} 
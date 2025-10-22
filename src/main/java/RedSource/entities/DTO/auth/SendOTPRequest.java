package RedSource.entities.DTO.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendOTPRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(63\\d{10}|\\d{10})$", message = "Phone number must be 10 digits or 12 digits with 63 prefix")
    private String phoneNumber;
} 
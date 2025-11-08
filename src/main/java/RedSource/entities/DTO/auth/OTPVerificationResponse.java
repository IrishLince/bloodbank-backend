package RedSource.entities.DTO.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OTPVerificationResponse {
    private boolean success;
    private String message;
    private int attempts;
    private int maxAttempts;
    private boolean maxAttemptsReached;
    private boolean otpExpired;
} 
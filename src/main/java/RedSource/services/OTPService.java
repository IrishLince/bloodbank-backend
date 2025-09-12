package RedSource.services;

import RedSource.entities.OTP;
import RedSource.entities.DTO.auth.OTPVerificationResponse;
import RedSource.repositories.OTPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Optional;

@Service
public class OTPService {
    
    private static final int MAX_ATTEMPTS = 3;
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Autowired
    private OTPRepository otpRepository;
    
    @Autowired
    private SNSService snsService;
    
    @Value("${otp.length:6}")
    private int otpLength;
    
    @Value("${otp.expiration.minutes:5}")
    private int expirationMinutes;
    
    public String generateAndSendOTP(String phoneNumber, String email) {
        // Delete any existing OTP for this phone number and email
        otpRepository.deleteByPhoneNumberAndEmail(phoneNumber, email);
        
        // Generate new OTP
        String otpCode = generateOTP();
        
        // Save to database
        OTP otp = new OTP(phoneNumber, otpCode, email);
        otpRepository.save(otp);
        
        // Send SMS
        String message = snsService.formatOTPMessage(otpCode);
        boolean smsSent = snsService.sendSMS(phoneNumber, message);
        
        if (!smsSent) {
            // If SMS failed, delete the OTP from database
            otpRepository.delete(otp);
            throw new RuntimeException("Failed to send OTP SMS");
        }
        
        String responseMessage = snsService.isTestingMode() 
            ? "OTP generated successfully for testing (no SMS sent to save AWS credits). Check console logs for OTP details."
            : "OTP sent successfully to +63" + phoneNumber;
            
        return responseMessage;
    }
    
    public OTPVerificationResponse verifyOTP(String phoneNumber, String otpCode, String email) {
        // Find the OTP record
        Optional<OTP> otpOpt = otpRepository.findByPhoneNumberAndEmailAndVerifiedFalse(phoneNumber, email);
        
        if (!otpOpt.isPresent()) {
            return new OTPVerificationResponse(
                false, 
                "No valid OTP found. Please request a new OTP.",
                0,
                MAX_ATTEMPTS,
                false,
                true
            );
        }
        
        OTP otp = otpOpt.get();
        
        // Check if OTP has expired
        if (otp.getCreatedAt().before(new Date(System.currentTimeMillis() - (expirationMinutes * 60 * 1000)))) {
            otpRepository.delete(otp);
            return new OTPVerificationResponse(
                false, 
                "OTP has expired. Please request a new OTP.",
                otp.getAttempts(),
                MAX_ATTEMPTS,
                false,
                true
            );
        }
        
        // Verify OTP code
        if (otp.getOtpCode().equals(otpCode)) {
            // Mark as verified but DON'T delete yet - keep for signup validation
            otp.setVerified(true);
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);
            
            return new OTPVerificationResponse(
                true, 
                "OTP verified successfully",
                otp.getAttempts(),
                MAX_ATTEMPTS,
                false,
                false
            );
        } else {
            // Increment attempts
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);
            
            boolean maxReached = otp.getAttempts() >= MAX_ATTEMPTS;
            if (maxReached) {
                otpRepository.delete(otp);
            }
            
            return new OTPVerificationResponse(
                false, 
                maxReached ? "Maximum attempts reached. Please request a new OTP." : "Invalid OTP code. Please try again.",
                otp.getAttempts(),
                MAX_ATTEMPTS,
                maxReached,
                false
            );
        }
    }
    
    public boolean isOTPVerified(String phoneNumber, String email) {
        Optional<OTP> otpOpt = otpRepository.findByPhoneNumberAndEmailAndVerifiedTrue(phoneNumber, email);
        return otpOpt.isPresent();
    }
    
    public void cleanupVerifiedOTP(String phoneNumber, String email) {
        otpRepository.deleteByPhoneNumberAndEmail(phoneNumber, email);
    }
    
    public OTPVerificationResponse verifyOTPForPasswordReset(String phoneNumber, String otpCode, String email) {
        // Find the OTP record - look for verified OTPs since verification step already marked it as verified
        Optional<OTP> otpOpt = otpRepository.findByPhoneNumberAndEmailAndVerifiedTrue(phoneNumber, email);
        
        if (!otpOpt.isPresent()) {
            // If no verified OTP found, also check for unverified ones as fallback
            otpOpt = otpRepository.findByPhoneNumberAndEmailAndVerifiedFalse(phoneNumber, email);
            
            if (!otpOpt.isPresent()) {
                return new OTPVerificationResponse(
                    false, 
                    "No valid OTP found. Please request a new OTP.",
                    0,
                    MAX_ATTEMPTS,
                    false,
                    true
                );
            }
        }
        
        OTP otp = otpOpt.get();
        
        // Check if OTP has expired
        if (otp.getCreatedAt().before(new Date(System.currentTimeMillis() - (expirationMinutes * 60 * 1000)))) {
            otpRepository.delete(otp);
            return new OTPVerificationResponse(
                false, 
                "OTP has expired. Please request a new OTP.",
                otp.getAttempts(),
                MAX_ATTEMPTS,
                false,
                true
            );
        }
        
        // Verify OTP code
        if (otp.getOtpCode().equals(otpCode)) {
            // OTP is correct - mark as verified and return success
            otp.setVerified(true);
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);
            
            return new OTPVerificationResponse(
                true, 
                "OTP verified successfully for password reset",
                otp.getAttempts(),
                MAX_ATTEMPTS,
                false,
                false
            );
        } else {
            // Increment attempts
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);
            
            boolean maxReached = otp.getAttempts() >= MAX_ATTEMPTS;
            if (maxReached) {
                otpRepository.delete(otp);
            }
            
            return new OTPVerificationResponse(
                false, 
                maxReached ? "Maximum attempts reached. Please request a new OTP." : "Invalid OTP code. Please try again.",
                otp.getAttempts(),
                MAX_ATTEMPTS,
                maxReached,
                false
            );
        }
    }
    
    private String generateOTP() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }
} 
package RedSource.services;

import RedSource.entities.OTP;
import RedSource.entities.DTO.auth.OTPVerificationResponse;
import RedSource.repositories.OTPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
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
    private SemaphoreSMSService semaphoreSMSService;

    @Value("${otp.length:6}")
    private int otpLength;

    @Value("${otp.expiration.minutes:5}")
    private int expirationMinutes;

    public String generateAndSendOTP(String phoneNumber, String email) {
        return generateAndSendOTP(phoneNumber, email, SemaphoreSMSService.OTPType.GENERAL, null);
    }

    public String generateAndSendOTP(String phoneNumber, String email, SemaphoreSMSService.OTPType otpType,
            String userName) {
        // Delete any existing OTP for this phone number and email
        otpRepository.deleteByPhoneNumberAndEmail(phoneNumber, email);

        // Generate placeholder OTP for message template (will be replaced by Semaphore)
        String placeholderOtpCode = generateOTP();

        // Send SMS using Semaphore with specific template
        String message = semaphoreSMSService.formatOTPMessage(placeholderOtpCode, otpType, userName);
        SemaphoreSMSService.SemaphoreOTPResponse otpResponse = semaphoreSMSService.sendOTP(phoneNumber, message);
        boolean smsSent = otpResponse.isSuccess();

        if (!smsSent) {
            throw new RuntimeException("Failed to send OTP SMS: " + otpResponse.getMessage());
        }

        // Use the actual OTP code returned by Semaphore (not our placeholder)
        String actualOtpCode = otpResponse.getOtpCode();
        if (actualOtpCode == null || actualOtpCode.isEmpty()) {
            // Fallback to our generated code if Semaphore doesn't return one (testing mode)
            actualOtpCode = placeholderOtpCode;
        }

        // Save the actual OTP code to database
        OTP otp = new OTP(phoneNumber, actualOtpCode, email);
        otpRepository.save(otp);

        String responseMessage = semaphoreSMSService.isTestingMode()
                ? "OTP generated successfully for testing (no Semaphore charges). Check console logs for OTP details. Code: "
                        + actualOtpCode
                : "OTP sent successfully to " + phoneNumber;

        return responseMessage;
    }

    public OTPVerificationResponse verifyOTP(String phoneNumber, String otpCode, String email) {
        // Find the OTP record - check both verified and unverified OTPs
        Optional<OTP> otpOpt = otpRepository.findByPhoneNumberAndEmailAndVerifiedFalse(phoneNumber, email);

        // If no unverified OTP found, check for verified ones (for multi-step
        // processes)
        if (!otpOpt.isPresent()) {
            otpOpt = otpRepository.findByPhoneNumberAndEmailAndVerifiedTrue(phoneNumber, email);
        }

        if (!otpOpt.isPresent()) {
            return new OTPVerificationResponse(
                    false,
                    "No valid OTP found. Please request a new OTP.",
                    0,
                    MAX_ATTEMPTS,
                    false,
                    true);
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
                    true);
        }

        // Verify OTP code
        if (otp.getOtpCode().equals(otpCode)) {
            // OTP is correct - DELETE it immediately after successful verification
            otpRepository.delete(otp);

            return new OTPVerificationResponse(
                    true,
                    "OTP verified successfully",
                    otp.getAttempts() + 1,
                    MAX_ATTEMPTS,
                    false,
                    false);
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
                    maxReached ? "Maximum attempts reached. Please request a new OTP."
                            : "Invalid OTP code. Please try again.",
                    otp.getAttempts(),
                    MAX_ATTEMPTS,
                    maxReached,
                    false);
        }
    }

    /**
     * Verify OTP without deleting it (for multi-step processes)
     * Used when OTP verification is separate from the final action
     */
    public OTPVerificationResponse verifyOTPWithoutDelete(String phoneNumber, String otpCode, String email) {
        // Find the OTP record
        Optional<OTP> otpOpt = otpRepository.findByPhoneNumberAndEmailAndVerifiedFalse(phoneNumber, email);

        if (!otpOpt.isPresent()) {
            return new OTPVerificationResponse(
                    false,
                    "No valid OTP found. Please request a new OTP.",
                    0,
                    MAX_ATTEMPTS,
                    false,
                    true);
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
                    true);
        }

        // Verify OTP code
        if (otp.getOtpCode().equals(otpCode)) {
            // OTP is correct - mark as verified but DON'T delete (for multi-step process)
            otp.setVerified(true);
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);

            return new OTPVerificationResponse(
                    true,
                    "OTP verified successfully",
                    otp.getAttempts(),
                    MAX_ATTEMPTS,
                    false,
                    false);
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
                    maxReached ? "Maximum attempts reached. Please request a new OTP."
                            : "Invalid OTP code. Please try again.",
                    otp.getAttempts(),
                    MAX_ATTEMPTS,
                    maxReached,
                    false);
        }
    }

    public void cleanupVerifiedOTP(String phoneNumber, String email) {
        otpRepository.deleteByPhoneNumberAndEmail(phoneNumber, email);
    }

    public OTPVerificationResponse verifyOTPForPasswordReset(String phoneNumber, String otpCode, String email) {
        // Find the OTP record for password reset - same logic as regular OTP
        // verification
        Optional<OTP> otpOpt = otpRepository.findByPhoneNumberAndEmailAndVerifiedFalse(phoneNumber, email);

        if (!otpOpt.isPresent()) {

            return new OTPVerificationResponse(
                    false,
                    "No valid OTP found. Please request a new OTP.",
                    0,
                    MAX_ATTEMPTS,
                    false,
                    true);
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
                    true);
        }

        // Verify OTP code
        if (otp.getOtpCode().equals(otpCode)) {
            // OTP is correct - DELETE it immediately after successful verification
            otpRepository.delete(otp);

            return new OTPVerificationResponse(
                    true,
                    "OTP verified successfully for password reset",
                    otp.getAttempts() + 1,
                    MAX_ATTEMPTS,
                    false,
                    false);
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
                    maxReached ? "Maximum attempts reached. Please request a new OTP."
                            : "Invalid OTP code. Please try again.",
                    otp.getAttempts(),
                    MAX_ATTEMPTS,
                    maxReached,
                    false);
        }
    }

    private String generateOTP() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Scheduled cleanup task to remove expired OTPs from database
     * Runs every 5 minutes to clean up expired OTPs
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
    public void cleanupExpiredOTPs() {
        try {
            Date expirationTime = new Date(System.currentTimeMillis() - (expirationMinutes * 60 * 1000));

            // Find and delete all expired OTPs
            otpRepository.findAll().forEach(otp -> {
                if (otp.getCreatedAt().before(expirationTime)) {
                    otpRepository.delete(otp);
                    System.out.println("Deleted expired OTP for phone: " + otp.getPhoneNumber() + " created at: "
                            + otp.getCreatedAt());
                }
            });
        } catch (Exception e) {
            System.err.println("Error during OTP cleanup: " + e.getMessage());
        }
    }
}
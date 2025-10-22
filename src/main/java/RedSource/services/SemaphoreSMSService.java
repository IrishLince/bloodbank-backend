package RedSource.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class SemaphoreSMSService {
    private static final Logger logger = Logger.getLogger(SemaphoreSMSService.class.getName());

    // OTP Types for different message templates
    public enum OTPType {
        REGISTRATION,
        FORGOT_PASSWORD,
        CHANGE_PASSWORD,
        CHANGE_PHONE,
        GENERAL
    }

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${semaphore.sms.api.key}")
    private String apiKey;

    @Value("${semaphore.sms.api.url:https://api.semaphore.co/api/v4/otp}")
    private String apiUrl;

    @Value("${semaphore.sms.sender.name:SEMAPHORE}")
    private String senderName;

    @Value("${sms.testing.mode:true}")
    private boolean testingMode;

    public SemaphoreSMSService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Send OTP using Semaphore SMS API
     * 
     * @param phoneNumber The phone number to send OTP to
     * @param message     Custom message (optional, will use default if null/empty)
     * @return SemaphoreOTPResponse containing OTP details
     */
    public SemaphoreOTPResponse sendOTP(String phoneNumber, String message) {
        try {
            // Format phone number for Semaphore API (Philippine format)
            String formattedPhoneNumber = formatPhoneNumber(phoneNumber);

            if (testingMode) {
                logger.info("SMS sending is in testing mode. Would send OTP to " + formattedPhoneNumber);
                logger.info("Testing mode message: " + message);
                // Return a mock response for testing using the actual message template
                return new SemaphoreOTPResponse(true, "OTP sent successfully (testing mode)",
                        "123456", System.currentTimeMillis() + (5 * 60 * 1000), false, formattedPhoneNumber,
                        message != null ? message : "Default OTP message");
            }

            // Prepare request body for Semaphore OTP API
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("apikey", apiKey);
            requestBody.put("number", formattedPhoneNumber);
            // Use the message as-is since it already contains {otp} placeholder from
            // formatOTPMessage
            String otpMessage = message != null ? message : "Your verification code is: {otp}";
            requestBody.put("message", otpMessage);
            requestBody.put("sendername", senderName);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Convert to form data with proper URL encoding
            StringBuilder formData = new StringBuilder();
            for (Map.Entry<String, String> entry : requestBody.entrySet()) {
                if (formData.length() > 0) {
                    formData.append("&");
                }
                formData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }

            logger.info("Semaphore API Request URL: " + apiUrl); // Debug logging
            logger.info("Semaphore API Request Data: " + formData.toString()); // Debug logging

            HttpEntity<String> request = new HttpEntity<>(formData.toString(), headers);

            // Make API call
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                logger.info("Semaphore API Response: " + responseBody); // Debug logging
                
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                // Semaphore OTP API returns an array with OTP details
                if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                    JsonNode otpData = jsonResponse.get(0);
                    logger.info("OTP Data: " + otpData.toString()); // Debug logging

                    if (otpData.has("message_id") && otpData.has("code")) {
                        String otpCode = otpData.get("code").asText();
                        String sentMessage = otpData.has("message") ? otpData.get("message").asText() : message;

                        return new SemaphoreOTPResponse(
                                true,
                                "OTP sent successfully via Semaphore",
                                otpCode,
                                System.currentTimeMillis() + (5 * 60 * 1000), // 5 minutes expiration
                                false,
                                formattedPhoneNumber,
                                sentMessage);
                    }
                }

                // Handle error response - check if it's an object with error details
                String errorMessage = "Unknown error from Semaphore OTP API";
                if (jsonResponse.has("message")) {
                    errorMessage = jsonResponse.get("message").asText();
                } else if (jsonResponse.has("error")) {
                    errorMessage = jsonResponse.get("error").asText();
                } else if (jsonResponse.has("errors")) {
                    // Handle validation errors array
                    JsonNode errors = jsonResponse.get("errors");
                    if (errors.isArray() && errors.size() > 0) {
                        errorMessage = errors.get(0).asText();
                    }
                } else {
                    // Log the full response for debugging
                    errorMessage = "API Error - Response: " + responseBody;
                }

                logger.severe("Semaphore OTP API returned error: " + errorMessage);
                return new SemaphoreOTPResponse(false, errorMessage,
                        null, 0, false, formattedPhoneNumber, null);
            } else {
                String errorBody = response.getBody();
                logger.severe("HTTP error from Semaphore OTP API: " + response.getStatusCode() + " - Body: " + errorBody);
                
                String errorMessage = "Failed to send OTP: HTTP " + response.getStatusCode();
                
                // Try to parse error response for more details
                try {
                    if (errorBody != null && !errorBody.isEmpty()) {
                        JsonNode errorJson = objectMapper.readTree(errorBody);
                        if (errorJson.has("message")) {
                            errorMessage = errorJson.get("message").asText();
                        } else if (errorJson.has("error")) {
                            errorMessage = errorJson.get("error").asText();
                        }
                    }
                } catch (Exception e) {
                    // If we can't parse the error, use the default message
                    logger.warning("Could not parse error response: " + e.getMessage());
                }
                
                return new SemaphoreOTPResponse(false, errorMessage,
                        null, 0, false, formattedPhoneNumber, null);
            }

        } catch (Exception e) {
            logger.severe("Error sending OTP via Semaphore: " + e.getMessage());
            return new SemaphoreOTPResponse(false, "Failed to send OTP: " + e.getMessage(),
                    null, 0, false, phoneNumber, null);
        }
    }

    /**
     * Verify OTP using Semaphore SMS API
     * Note: Semaphore doesn't have built-in OTP verification, so we handle this
     * internally
     * 
     * @param phoneNumber The phone number
     * @param otpCode     The OTP code to verify
     * @return SemaphoreVerifyResponse containing verification result
     */
    public SemaphoreVerifyResponse verifyOTP(String phoneNumber, String otpCode) {
        try {
            // Format phone number for Semaphore API
            String formattedPhoneNumber = formatPhoneNumber(phoneNumber);

            if (testingMode) {
                logger.info("OTP verification is in testing mode. Would verify " + otpCode + " for "
                        + formattedPhoneNumber);
                // Return success for testing mode if OTP is "123456"
                boolean isValid = "123456".equals(otpCode);
                return new SemaphoreVerifyResponse(isValid,
                        isValid ? "OTP verified successfully (testing mode)" : "Invalid OTP (testing mode)");
            }

            // Since Semaphore doesn't have built-in OTP verification,
            // we rely on our internal OTP storage and verification logic
            // This method is mainly for consistency with the interface
            return new SemaphoreVerifyResponse(true, "OTP verification handled internally");

        } catch (Exception e) {
            return new SemaphoreVerifyResponse(false, "Failed to verify OTP: " + e.getMessage());
        }
    }

    /**
     * Format OTP message for SMS using specific templates with {otp} placeholder
     * 
     * @param otpCode  The OTP code (will be replaced with {otp} placeholder for
     *                 Semaphore)
     * @param otpType  The type of OTP (determines message template)
     * @param userName Optional user name for personalization
     * @return Formatted message with {otp} placeholder
     */
    public String formatOTPMessage(String otpCode, OTPType otpType, String userName) {
        String name = (userName != null && !userName.trim().isEmpty()) ? userName : "User";

        switch (otpType) {
            case REGISTRATION:
                return String.format(
                        "Hi %s! Use this code to verify your account: {otp}. This code will expire in 5 minutes. Do not share it with anyone.",
                        name);

            case FORGOT_PASSWORD:
                return "You requested to reset your password. Use this code: {otp}. It will expire in 5 minutes. Ignore this if you did not request it.";

            case CHANGE_PASSWORD:
                return "To confirm your password change, enter this OTP: {otp}. Expires in 5 minutes. Do not share this code.";

            case CHANGE_PHONE:
                return "You're updating your phone number. Use this OTP: {otp} to verify the change. This code will expire in 5 minutes.";

            case GENERAL:
            default:
                return "Your BloodBank verification code is: {otp}. This code will expire in 5 minutes. Do not share this code with anyone.";
        }
    }

    /**
     * Format OTP message for SMS (backward compatibility)
     * 
     * @param otpCode The OTP code (ignored, uses {otp} placeholder)
     * @return Formatted message using GENERAL template with {otp} placeholder
     */
    public String formatOTPMessage(String otpCode) {
        return formatOTPMessage(otpCode, OTPType.GENERAL, null);
    }

    /**
     * Check if SMS service is in testing mode
     * 
     * @return true if in testing mode, false if in production mode
     */
    public boolean isTestingMode() {
        return testingMode;
    }

    /**
     * Get the current mode description
     * 
     * @return String describing current mode
     */
    public String getCurrentModeDescription() {
        return testingMode ? "TESTING MODE - SMS simulation (no Semaphore charges)"
                : "PRODUCTION MODE - Actual SMS sending (Semaphore charges apply)";
    }

    /**
     * Format phone number for Semaphore API (Philippine format)
     * 
     * @param phoneNumber Input phone number
     * @return Formatted phone number
     */
    private String formatPhoneNumber(String phoneNumber) {
        // Remove any existing + prefix
        String cleanNumber = phoneNumber.startsWith("+") ? phoneNumber.substring(1) : phoneNumber;

        // Handle different input formats:
        if (cleanNumber.startsWith("63") && cleanNumber.length() == 12) {
            // Already in Philippine E.164 format (e.g., 639123456789)
            return "+" + cleanNumber; // Convert to +639123456789
        } else if (cleanNumber.length() == 11 && cleanNumber.startsWith("0")) {
            // Philippine local number with leading 0 (e.g., 09123456789)
            return "+63" + cleanNumber.substring(1); // Convert to +639123456789
        } else if (cleanNumber.length() == 10 && cleanNumber.startsWith("9")) {
            // Philippine number without country code and leading 0 (e.g., 9123456789)
            return "+63" + cleanNumber; // Convert to +639123456789
        } else {
            // For any other format, assume it needs +63 prefix
            logger.warning("Unrecognized phone number format: " + phoneNumber + ", adding +63 prefix");
            return "+63" + cleanNumber;
        }
    }

    /**
     * Response class for Semaphore OTP send operation
     */
    public static class SemaphoreOTPResponse {
        private final boolean success;
        private final String message;
        private final String otpCode;
        private final long expiresAt;
        private final boolean confirmed;
        private final String phoneNumber;
        private final String smsMessage;

        public SemaphoreOTPResponse(boolean success, String message, String otpCode,
                long expiresAt, boolean confirmed, String phoneNumber, String smsMessage) {
            this.success = success;
            this.message = message;
            this.otpCode = otpCode;
            this.expiresAt = expiresAt;
            this.confirmed = confirmed;
            this.phoneNumber = phoneNumber;
            this.smsMessage = smsMessage;
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getOtpCode() {
            return otpCode;
        }

        public long getExpiresAt() {
            return expiresAt;
        }

        public boolean isConfirmed() {
            return confirmed;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getSmsMessage() {
            return smsMessage;
        }
    }

    /**
     * Response class for Semaphore OTP verify operation
     */
    public static class SemaphoreVerifyResponse {
        private final boolean success;
        private final String message;

        public SemaphoreVerifyResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}

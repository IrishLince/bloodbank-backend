package RedSource.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;

import java.time.Duration;

import java.util.logging.Logger;

@Service
public class SNSService {
    private static final Logger logger = Logger.getLogger(SNSService.class.getName());
    
    private final SnsClient snsClient;
    
    @Value("${sms.testing.mode:true}")
    private boolean testingMode;
    
    public SNSService(@Value("${aws.region}") String region,
                     @Value("${aws.sns.access-key}") String accessKey,
                     @Value("${aws.sns.secret-key}") String secretKey) {
        
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        // Configure client with timeout settings to prevent hanging
        ClientOverrideConfiguration clientConfig = ClientOverrideConfiguration.builder()
                .apiCallTimeout(Duration.ofSeconds(30))  // Total timeout for API call
                .apiCallAttemptTimeout(Duration.ofSeconds(10))  // Timeout per attempt
                .build();
        
        this.snsClient = SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .overrideConfiguration(clientConfig)
                .build();
    }
    
    public boolean sendSMS(String phoneNumber, String message) {
        try {
            // Fix phone number format for AWS SNS
            String formattedPhoneNumber;
            
            // Remove any existing + prefix
            String cleanNumber = phoneNumber.startsWith("+") ? phoneNumber.substring(1) : phoneNumber;
            
            // Handle different input formats:
            // 639394123330 (12 digits) -> +639394123330
            // 9394123330 (10 digits) -> +639394123330
            if (cleanNumber.startsWith("63") && cleanNumber.length() == 12) {
                formattedPhoneNumber = "+" + cleanNumber; // +639394123330
            } else if (cleanNumber.length() == 10) {
                formattedPhoneNumber = "+63" + cleanNumber; // +639394123330    
            } else {
                // Invalid format, log and return false
                logger.severe("Invalid phone number format: " + phoneNumber + " (expected 10 or 12 digits)");
                return false;
            }
            
            // Check if we're in testing mode to avoid consuming AWS credits
            if (testingMode) {
                logger.info("TESTING MODE: SMS not actually sent (saving AWS credits)");
                logger.info("Would send to: " + formattedPhoneNumber);
                logger.info("Message content: " + message);
                logger.info("To enable actual SMS sending, set sms.testing.mode=false in application.properties");
                return true; // Return success to allow normal application flow
            }
            
            // Production mode - actually send SMS
            logger.info("PRODUCTION MODE: Sending actual SMS to: " + formattedPhoneNumber);
            
            PublishRequest request = PublishRequest.builder()
                    .phoneNumber(formattedPhoneNumber)
                    .message(message)
                    .build();
            
            PublishResponse response = snsClient.publish(request);
            
            logger.info("✅ SMS sent successfully! Message ID: " + response.messageId() + " to " + formattedPhoneNumber);
            return true;
            
        } catch (SnsException e) {
            logger.severe("Failed to send SMS: " + e.getMessage());
            logger.severe("AWS Error Code: " + e.awsErrorDetails().errorCode());
            logger.severe("AWS Error Message: " + e.awsErrorDetails().errorMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            logger.severe("Unexpected error sending SMS: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
               public String formatOTPMessage(String otpCode) {
               return String.format("Your BloodBank verification code is: %s. This code will expire in 5 minutes. Do not share this code with anyone.", otpCode);
           }
    
    /**
     * Check if SMS service is in testing mode
     * @return true if in testing mode (no actual SMS sent), false if in production mode (actual SMS sent)
     */
    public boolean isTestingMode() {
        return testingMode;
    }
    
    /**
     * Get the current mode description
     * @return String describing current mode
     */
    public String getCurrentModeDescription() {
        return testingMode ? "TESTING MODE - SMS simulation (no AWS charges)" : "PRODUCTION MODE - Actual SMS sending (AWS charges apply)";
    }
} 
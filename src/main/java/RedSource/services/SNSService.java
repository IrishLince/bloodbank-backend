package RedSource.services;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class SNSService {
    private static final Logger logger = Logger.getLogger(SNSService.class.getName());
    
    private final AmazonSNS snsClient;
    
    @Value("${sms.testing.mode:true}")
    private boolean testingMode;
    
    public SNSService(@Value("${aws.region}") String region,
                     @Value("${aws.sns.access-key}") String accessKey,
                     @Value("${aws.sns.secret-key}") String secretKey) {
        
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        
        this.snsClient = AmazonSNSClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
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
            if (cleanNumber.startsWith("1") && cleanNumber.length() == 11) {
                // US/Canada number in E.164 format (e.g., 12065551212)
                formattedPhoneNumber = "+" + cleanNumber;
            } else if (cleanNumber.startsWith("63") && cleanNumber.length() == 12) {
                // Philippine number in E.164 format (e.g., 639123456789)
                formattedPhoneNumber = "+" + cleanNumber;
            } else if (cleanNumber.length() == 10 && !cleanNumber.startsWith("0")) {
                // Assuming US/Canada local number without country code (e.g., 2065551212)
                formattedPhoneNumber = "+1" + cleanNumber;
            } else if (cleanNumber.length() == 11 && cleanNumber.startsWith("0")) {
                // Assuming Philippine local number with leading 0 (e.g., 09123456789)
                formattedPhoneNumber = "+63" + cleanNumber.substring(1);
            } else if (cleanNumber.length() == 10 && cleanNumber.startsWith("9")) {
                // Assuming Philippine number without country code and leading 0 (e.g., 9123456789)
                formattedPhoneNumber = "+63" + cleanNumber;
            } else {
                // For any other format, just add + and hope for the best
                formattedPhoneNumber = "+" + cleanNumber;
                logger.warning("Unrecognized phone number format: " + phoneNumber);
            }
            
            if (testingMode) {
                logger.info("SMS sending is in testing mode. Would send to " + formattedPhoneNumber + ": " + message);
                return true;
            }
            
            try {
                PublishRequest request = new PublishRequest()
                    .withPhoneNumber(formattedPhoneNumber)
                    .withMessage(message);
                
                PublishResult result = snsClient.publish(request);
                logger.info("SMS sent to " + formattedPhoneNumber + ". Message ID: " + result.getMessageId());
                return true;
            } catch (Exception e) {
                logger.severe("Error sending SMS: " + e.getMessage());
                return false;
            }
        } catch (Exception e) {
            logger.severe("Unexpected error in sendSMS: " + e.getMessage());
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
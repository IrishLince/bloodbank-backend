package RedSource.controllers;

import RedSource.entities.User;
import RedSource.entities.UserAdmin;
import RedSource.entities.Hospital;
import RedSource.entities.BloodBankUser;
import RedSource.entities.DTO.auth.JwtResponse;
import RedSource.entities.DTO.auth.LoginRequest;
import RedSource.entities.DTO.auth.MessageResponse;
import RedSource.entities.DTO.auth.OTPVerificationResponse;
import RedSource.entities.DTO.auth.SignupRequest;
import RedSource.entities.DTO.auth.SendOTPRequest;
import RedSource.entities.DTO.auth.VerifyOTPRequest;
import RedSource.entities.DTO.auth.TokenRefreshRequest;
import RedSource.entities.DTO.auth.TokenRefreshResponse;
import RedSource.entities.enums.UserRoleType;
import RedSource.repositories.UserRepository;
import RedSource.repositories.UserAdminRepository;
import RedSource.repositories.HospitalRepository;
import RedSource.repositories.BloodBankUserRepository;
import RedSource.security.JwtUtils;
import RedSource.services.TokenService;
import RedSource.services.OTPService;
import RedSource.services.SemaphoreSMSService;
import RedSource.services.AppointmentService;
import RedSource.security.UserDetailsImpl;
import RedSource.services.FileStorageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE, RequestMethod.OPTIONS })
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    // Synchronization map to prevent concurrent login attempts for same user
    private final ConcurrentHashMap<String, Object> loginLocks = new ConcurrentHashMap<>();

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserAdminRepository userAdminRepository;

    @Autowired
    HospitalRepository hospitalRepository;

    @Autowired
    BloodBankUserRepository bloodBankUserRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    TokenService tokenService;

    @Autowired
    OTPService otpService;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    AppointmentService appointmentService;

    /**
     * Utility method to normalize phone numbers consistently
     * @param phone Raw phone number
     * @return Normalized phone number in 12-digit format (63XXXXXXXXXX)
     */
    private String normalizePhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        
        String cleanPhone = phone.replaceAll("\\D", ""); // Remove all non-digits
        
        if (cleanPhone.length() == 10) {
            return "63" + cleanPhone; // Add country code
        } else if (cleanPhone.length() == 12 && cleanPhone.startsWith("63")) {
            return cleanPhone; // Already in correct format
        } else if (cleanPhone.length() == 11 && cleanPhone.startsWith("9")) {
            return "63" + cleanPhone; // Handle 9XXXXXXXXXX format
        }
        
        return cleanPhone; // Return as-is for other cases
    }

    /**
     * Utility method to get 10-digit phone format (without country code)
     * @param phone Normalized phone number
     * @return 10-digit format or original if invalid
     */
    private String getShortPhoneFormat(String phone) {
        if (phone != null && phone.length() == 12 && phone.startsWith("63")) {
            return phone.substring(2);
        }
        return phone;
    }

    /**
     * Optimized helper method to find user from any collection (users_admin, users, users_hospital, users_bloodbank)
     * Uses more efficient search pattern with admin-first priority
     */
    private User findUserByEmail(String email) {
        // First, try admin users collection (NEW - highest priority)
        Optional<UserAdmin> adminOpt = userAdminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            UserAdmin admin = adminOpt.get();
            // Convert UserAdmin to User for consistency
            return User.builder()
                    .id(admin.getId())
                    .name(admin.getName())
                    .email(admin.getEmail())
                    .username(admin.getUsername())
                    .password(admin.getPassword())
                    .role(UserRoleType.ADMIN)
                    .build();
        }

        // Then, try regular users collection
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        // Then, try hospitals collection
        Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(email);
        if (hospitalOpt.isPresent()) {
            Hospital hospital = hospitalOpt.get();
            // Convert Hospital to User for consistency
            return User.builder()
                    .id(hospital.getId())
                    .name(hospital.getHospitalName())
                    .email(hospital.getEmail())
                    .username(hospital.getUsername())
                    .password(hospital.getPassword())
                    .role(UserRoleType.HOSPITAL)
                    .contactInformation(hospital.getPhone())
                    .profilePhotoUrl(hospital.getProfilePhotoUrl())
                    .createdAt(hospital.getCreatedAt())
                    .updatedAt(hospital.getUpdatedAt())
                    .build();
        }

        // Finally, try blood bank users collection
        Optional<BloodBankUser> bloodBankUserOpt = bloodBankUserRepository.findByEmail(email);
        if (bloodBankUserOpt.isPresent()) {
            BloodBankUser bloodBankUser = bloodBankUserOpt.get();
            // Convert BloodBankUser to User for consistency
            return User.builder()
                    .id(bloodBankUser.getId())
                    .name(bloodBankUser.getBloodBankName())
                    .email(bloodBankUser.getEmail())
                    .username(bloodBankUser.getUsername())
                    .password(bloodBankUser.getPassword())
                    .role(UserRoleType.BLOODBANK)
                    .contactInformation(bloodBankUser.getPhone())
                    .profilePhotoUrl(bloodBankUser.getProfilePhotoUrl())
                    .createdAt(bloodBankUser.getCreatedAt())
                    .updatedAt(bloodBankUser.getUpdatedAt())
                    .build();
        }

        throw new RuntimeException("User not found with email: " + email);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Get or create a lock for this user's email to prevent concurrent logins
        Object lock = loginLocks.computeIfAbsent(loginRequest.getEmail(), k -> new Object());

        // Synchronize on the lock to prevent concurrent logins for the same user
        synchronized (lock) {
            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                String jwt = jwtUtils.generateJwtToken(authentication);

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                List<String> roles = userDetails.getAuthorities().stream()
                        .map(item -> item.getAuthority())
                        .collect(Collectors.toList());

                // Generate refresh token
                String refreshToken = jwtUtils.generateRefreshToken(authentication);

                // Get the user entity to include in the response - check all collections
                User user = findUserByEmail(userDetails.getEmail());

                // Set token expiration dates
                Date accessTokenExpirationDate = jwtUtils.getExpirationDateFromToken(jwt);
                Date refreshTokenExpirationDate = jwtUtils.getExpirationDateFromToken(refreshToken);

                try {
                    tokenService.saveAccessToken(user, jwt, accessTokenExpirationDate);
                    tokenService.saveRefreshToken(user, refreshToken, refreshTokenExpirationDate);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to save authentication tokens", e);
                }

                return ResponseEntity.ok(new JwtResponse(jwt,
                        refreshToken,
                        userDetails.getId(),
                        user.getName(),
                        userDetails.getEmail(),
                        user.getProfilePhotoUrl(),
                        roles));
            } finally {
                // Clean up the lock after processing
                loginLocks.remove(loginRequest.getEmail());
            }
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        // Check if refresh token exists and is valid
        if (!tokenService.isTokenValid(requestRefreshToken)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Refresh token is not valid!"));
        }

        try {
            // Get username from refresh token
            String username = jwtUtils.getUserNameFromJwtToken(requestRefreshToken);

            // Get user from username using multi-collection logic
            User user = findUserByEmail(username);

            // Generate new access token
            String newAccessToken = jwtUtils.generateTokenFromUsername(username);

            // Save the new access token
            Date accessTokenExpirationDate = jwtUtils.getExpirationDateFromToken(newAccessToken);
            tokenService.saveAccessToken(user, newAccessToken, accessTokenExpirationDate);

            return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, requestRefreshToken));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: Could not refresh token"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Authentication required"));
            }

            // Use the same multi-collection logic as login
            User user = findUserByEmail(userDetails.getUsername());

            // Return user data based on role
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("username", user.getUsername());
            response.put("role", user.getRole().toString());
            response.put("contactInformation", user.getContactInformation());
            response.put("profilePhotoUrl", user.getProfilePhotoUrl());

            // Add role-specific data
            if (user.getRole() == UserRoleType.HOSPITAL) {
                // For hospitals, add additional hospital-specific fields if needed
                Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(user.getEmail());
                if (hospitalOpt.isPresent()) {
                    Hospital hospital = hospitalOpt.get();
                    response.put("hospitalName", hospital.getHospitalName());
                    response.put("hospitalId", hospital.getHospitalId());
                    response.put("licenseNumber", hospital.getLicenseNumber());
                    response.put("address", hospital.getAddress());
                    response.put("operatingHours", hospital.getOperatingHours());
                    response.put("isDonationCenter", hospital.getIsDonationCenter());
                }
            } else if (user.getRole() == UserRoleType.DONOR) {
                // Add donor-specific fields
                response.put("bloodType", user.getBloodType());
                response.put("dateOfBirth", user.getDateOfBirth());
                response.put("address", user.getAddress());
                response.put("sex", user.getSex());
                response.put("age", user.getAge());
                
                // Add reward points data
                response.put("rewardPoints", user.getRewardPoints() != null ? user.getRewardPoints() : 0);
                response.put("totalDonations", user.getTotalDonations() != null ? user.getTotalDonations() : 0);
                response.put("donorTier", user.getDonorTier() != null ? user.getDonorTier() : "NEW");
                
                // Add account status
                response.put("accountStatus", user.getAccountStatus());
                
                // Add account creation date (getter handles null fallback for legacy users)
                response.put("createdAt", user.getCreatedAt());
                
                // Get last donation date from completed appointments
                Date lastDonationDate = appointmentService.getLastCompletedDonationDate(user.getId());
                response.put("lastDonationDate", lastDonationDate);
            } else if (user.getRole() == UserRoleType.BLOODBANK) {
                // For blood banks, add additional blood bank-specific fields if needed
                Optional<BloodBankUser> bloodBankUserOpt = bloodBankUserRepository.findByEmail(user.getEmail());
                if (bloodBankUserOpt.isPresent()) {
                    BloodBankUser bloodBankUser = bloodBankUserOpt.get();
                    response.put("bloodBankName", bloodBankUser.getBloodBankName());
                    response.put("bloodBankId", bloodBankUser.getBloodBankId());
                    response.put("licenseNumber", bloodBankUser.getLicenseNumber());
                    response.put("address", bloodBankUser.getAddress());
                    response.put("operatingHours", bloodBankUser.getOperatingHours());
                }
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error fetching user profile: " + e.getMessage()));
        }
    }

    @PostMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Username is required"));
            }

            if (userRepository.existsByUsername(username)) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Username is already taken"));
            }

            return ResponseEntity.ok(new MessageResponse("Username is available"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error checking username: " + e.getMessage()));
        }
    }

    @RequestMapping(value = "/check-email", method = { RequestMethod.POST, RequestMethod.OPTIONS })
    public ResponseEntity<?> checkEmail(@RequestBody(required = false) Map<String, String> request,
            HttpServletRequest httpRequest) {
        // Handle OPTIONS preflight request
        if ("OPTIONS".equals(httpRequest.getMethod())) {
            return ResponseEntity.ok().build();
        }
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Email is required"));
            }

            if (userRepository.existsByEmail(email)) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Email is already in use"));
            }

            return ResponseEntity.ok(new MessageResponse("Email is available"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error checking email: " + e.getMessage()));
        }
    }

    @PostMapping("/check-phone")
    public ResponseEntity<?> checkPhone(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");
            if (phone == null || phone.trim().isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Phone number is required"));
            }

            // Normalize and validate phone number
            String normalizedPhone = normalizePhoneNumber(phone);
            String shortPhone = getShortPhoneFormat(normalizedPhone);
            
            if (normalizedPhone == null || (!normalizedPhone.matches("^63\\d{10}$"))) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Invalid phone number format. Expected format: 63XXXXXXXXXX"));
            }

            // Check both formats to catch all possible duplicates
            if (userRepository.existsByContactInformation(normalizedPhone) ||
                    userRepository.existsByContactInformation(shortPhone)) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Phone number is already in use"));
            }

            return ResponseEntity.ok(new MessageResponse("Phone number is available"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error checking phone: " + e.getMessage()));
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOTP(@Valid @RequestBody SendOTPRequest request) {
        try {
            // Check if email is already in use
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Email is already in use!"));
            }

            // Send registration OTP with user's name
            String result = otpService.generateAndSendOTP(
                    request.getPhoneNumber(),
                    request.getEmail(),
                    SemaphoreSMSService.OTPType.REGISTRATION,
                    "User" // Use generic name since SendOTPRequest doesn't have firstName
            );
            return ResponseEntity.ok(new MessageResponse(result));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Failed to send OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@Valid @RequestBody VerifyOTPRequest request) {
        try {
            OTPVerificationResponse response = otpService.verifyOTP(request.getPhoneNumber(), request.getOtpCode(),
                    request.getEmail());

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity
                        .badRequest()
                        .body(response);
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("OTP verification failed: " + e.getMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            // Check if email is already in use
            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Email is already in use!"));
            }

            // Check if username is already in use
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Username is already taken!"));
            }

            // Note: OTP verification is now handled separately via /verify-otp endpoint
            // OTPs are deleted immediately after successful verification for security

            // Create new user's account
            User user = User.builder()
                    .name(signUpRequest.getName())
                    .email(signUpRequest.getEmail())
                    .username(signUpRequest.getUsername())
                    .password(encoder.encode(signUpRequest.getPassword()))
                    .role(signUpRequest.getRole() != null ? signUpRequest.getRole() : UserRoleType.DONOR)
                    .contactInformation(normalizePhoneNumber(signUpRequest.getContactInformation()))
                    .bloodType(signUpRequest.getBloodType())
                    .address(signUpRequest.getAddress())
                    .age(signUpRequest.getAge())
                    .sex(signUpRequest.getSex())
                    .dateOfBirth(signUpRequest.getBirthDate())
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            userRepository.save(user);

            // Clean up the verified OTP - use normalized phone
            String normalizedPhone = normalizePhoneNumber(signUpRequest.getContactInformation());
            otpService.cleanupVerifiedOTP(normalizedPhone, signUpRequest.getEmail());

            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerUserWithPhoto(
            @RequestPart("data") @Valid SignupRequest signUpRequest,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        try {
            // Normalize phone for OTP check and persistence using utility method
            String normalizedPhone = normalizePhoneNumber(signUpRequest.getContactInformation());

            // Check if email is already in use
            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Email is already in use!"));
            }

            // Check if username is already in use
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Username is already taken!"));
            }

            // Note: OTP verification is now handled separately via /verify-otp endpoint
            // OTPs are deleted immediately after successful verification for security

            User user = User.builder()
                    .name(signUpRequest.getName())
                    .email(signUpRequest.getEmail())
                    .username(signUpRequest.getUsername())
                    .password(encoder.encode(signUpRequest.getPassword()))
                    .role(signUpRequest.getRole() != null ? signUpRequest.getRole() : UserRoleType.DONOR)
                    .contactInformation(normalizedPhone)
                    .bloodType(signUpRequest.getBloodType())
                    .address(signUpRequest.getAddress())
                    .age(signUpRequest.getAge())
                    .sex(signUpRequest.getSex())
                    .dateOfBirth(signUpRequest.getBirthDate())
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            // Store photo if provided
            String photoUrl = null;
            if (photo != null && !photo.isEmpty()) {
                try {
                    photoUrl = fileStorageService.storeUserPhoto(photo);
                    user.setProfilePhotoUrl(photoUrl);
                } catch (Exception ex) {
                    // If photo upload fails, clean up the uploaded file if any
                    if (photoUrl != null) {
                        try {
                            fileStorageService.deleteFile(photoUrl);
                        } catch (Exception deleteEx) {
                            // Log the error but continue with registration
                            System.err.println("Failed to clean up photo after upload error: " + deleteEx.getMessage());
                        }
                    }
                    // Proceed without photo if there's an error
                    user.setProfilePhotoUrl(null);
                }
            }

            try {
                userRepository.save(user);
                otpService.cleanupVerifiedOTP(normalizedPhone, signUpRequest.getEmail());
                return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
            } catch (Exception e) {
                // If user registration fails, clean up the uploaded photo
                if (photoUrl != null) {
                    try {
                        fileStorageService.deleteFile(photoUrl);
                    } catch (Exception deleteEx) {
                        System.err
                                .println("Failed to clean up photo after registration error: " + deleteEx.getMessage());
                    }
                }
                throw e;
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/send-password-otp")
    public ResponseEntity<?> sendPasswordOTP(@RequestBody Map<String, String> request) {
        try {

            String email = request.get("email");
            String phone = request.get("phone");

            if (email == null || email.trim().isEmpty()) {

                return ResponseEntity.badRequest().body(new MessageResponse("Email is required"));
            }

            if (phone == null || phone.trim().isEmpty()) {

                return ResponseEntity.badRequest().body(new MessageResponse("Phone number is required"));
            }

            // Verify user exists

            Optional<User> userOptional = userRepository.findByEmail(email);
            if (!userOptional.isPresent()) {

                return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
            }

            User user = userOptional.get();

            if (!phone.equals(user.getContactInformation())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Phone number does not match"));
            }

            // Format phone to 12-digit format for SMS sending (add 63 prefix if needed)
            String smsPhone = phone.length() == 10 ? "63" + phone : phone;

            // Send change password OTP with specific template
            String result = otpService.generateAndSendOTP(
                    smsPhone,
                    email,
                    SemaphoreSMSService.OTPType.CHANGE_PASSWORD,
                    "User");

            return ResponseEntity.ok(new MessageResponse(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error sending OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String phone = request.get("phone");
            String otpCode = request.get("otpCode");
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            // Validate input
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Email is required"));
            }

            if (phone == null || phone.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Phone number is required"));
            }

            if (otpCode == null || otpCode.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("OTP code is required"));
            }

            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Current password is required"));
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("New password is required"));
            }

            if (newPassword.length() < 8) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("New password must be at least 8 characters long"));
            }

            // Verify OTP - use same format as when OTP was sent (12-digit with country
            // code)
            String smsPhone = phone.length() == 10 ? "63" + phone : phone;
            OTPVerificationResponse otpResult = otpService.verifyOTP(smsPhone, otpCode, email); // Use same format as
                                                                                                // SMS sending
            if (!otpResult.isSuccess()) {
                return ResponseEntity.badRequest().body(new MessageResponse(otpResult.getMessage()));
            }

            // Find user
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (!userOptional.isPresent()) {
                return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
            }

            User user = userOptional.get();

            // Verify current password
            if (!encoder.matches(oldPassword, user.getPassword())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Current password is incorrect"));
            }

            // SECURITY: Check if new password is the same as current password
            if (encoder.matches(newPassword, user.getPassword())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("New password must be different from your current password"));
            }

            // Update password in MongoDB
            user.setPassword(encoder.encode(newPassword));
            user.setUpdatedAt(new Date());
            userRepository.save(user);

            // SECURITY: Invalidate all existing tokens for this user after password change
            // This forces logout on all devices for security purposes
            tokenService.revokeAllUserTokens(user.getId());

            // Clean up verified OTP after successful password change - use same format as
            // SMS
            otpService.cleanupVerifiedOTP(smsPhone, email);

            return ResponseEntity.ok(
                    new MessageResponse("Password changed successfully. Please log in again with your new password."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error changing password: " + e.getMessage()));
        }
    }

    @PostMapping("/send-phone-otp")
    public ResponseEntity<?> sendPhoneOTP(@RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Authentication required"));
            }

            String phoneNumber = request.get("phoneNumber");
            String userId = request.get("userId");

            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Phone number is required"));
            }

            // Validate phone number format (should be 12 digits: 639XXXXXXXXX)
            if (!phoneNumber.matches("^63\\d{10}$")) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Invalid phone number format. Expected format: 639XXXXXXXXX"));
            }

            // Check if phone number is already in use by another user
            Optional<User> existingUser = userRepository.findByContactInformation(phoneNumber);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userDetails.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Phone number is already in use"));
            }

            // Use user's email for OTP storage (since OTP system requires both phone and
            // email)
            String email = userDetails.getEmail();

            // Generate and send phone number change OTP
            String result;
            try {
                result = otpService.generateAndSendOTP(
                        phoneNumber,
                        email,
                        SemaphoreSMSService.OTPType.CHANGE_PHONE,
                        "User" // Use generic name since UserDetailsImpl doesn't have firstName/lastName
                );
            } catch (Exception smsException) {
                // Return a more specific error message
                String errorMessage = "Failed to send verification code. ";
                if (smsException.getMessage().contains("credentials") || smsException.getMessage().contains("api")) {
                    errorMessage += "Semaphore API issue. Please check your API configuration.";
                } else if (smsException.getMessage().contains("InvalidParameterValue") || smsException.getMessage().contains("invalid")) {
                    errorMessage += "Invalid phone number format for SMS service.";
                } else if (smsException.getMessage().contains("Throttling") || smsException.getMessage().contains("rate")) {
                    errorMessage += "Too many requests. Please wait a moment and try again.";
                } else {
                    errorMessage += "SMS service error: " + smsException.getMessage();
                }

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new MessageResponse(errorMessage));
            }

            return ResponseEntity.ok(new MessageResponse(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error sending phone verification OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/send-forgot-password-otp")
    public ResponseEntity<?> sendForgotPasswordOTP(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");

            if (phone == null || phone.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Phone number is required"));
            }

            // Format phone to match database format (10 digits without country code)
            String cleanPhone = phone.replaceAll("\\D", ""); // Remove all non-digits
            if (cleanPhone.startsWith("63") && cleanPhone.length() == 12) {
                cleanPhone = cleanPhone.substring(2); // Remove '63' prefix
            } else if (cleanPhone.length() != 10) {
                return ResponseEntity.badRequest().body(new MessageResponse(
                        "Invalid phone number format. Please enter a valid 10-digit phone number."));
            }

            // Check if user exists with this phone number
            Optional<User> userOptional = userRepository.findByContactInformation(cleanPhone);
            if (!userOptional.isPresent()) {
                // Try with 12-digit format as backup
                String phoneWith63 = "63" + cleanPhone;
                userOptional = userRepository.findByContactInformation(phoneWith63);

                if (!userOptional.isPresent()) {
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("No account found with this phone number"));
                }
            }

            User user = userOptional.get();

            // Format phone to 12-digit format for SMS sending
            // Use the format that was found in database for consistency
            String storedPhone = user.getContactInformation();
            String smsPhone;
            if (storedPhone.length() == 12 && storedPhone.startsWith("63")) {
                smsPhone = storedPhone; // Already in 12-digit format
            } else {
                smsPhone = "63" + storedPhone; // Add country code to 10-digit format
            }
            // Generate and send forgot password OTP
            String result = otpService.generateAndSendOTP(
                    smsPhone,
                    user.getEmail(),
                    SemaphoreSMSService.OTPType.FORGOT_PASSWORD,
                    null);

            return ResponseEntity.ok(new MessageResponse(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error sending forgot password OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-password-with-otp")
    public ResponseEntity<?> resetPasswordWithOTP(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");
            String otpCode = request.get("otpCode");
            String newPassword = request.get("newPassword");

            if (phone == null || phone.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Phone number is required"));
            }

            if (otpCode == null || otpCode.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("OTP code is required"));
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("New password is required"));
            }

            if (newPassword.length() < 8) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("New password must be at least 8 characters long"));
            }

            // Format phone to match database format (10 digits without country code)
            String cleanPhone = phone.replaceAll("\\D", ""); // Remove all non-digits
            if (cleanPhone.startsWith("63") && cleanPhone.length() == 12) {
                cleanPhone = cleanPhone.substring(2); // Remove '63' prefix
            } else if (cleanPhone.length() != 10) {
                return ResponseEntity.badRequest().body(new MessageResponse("Invalid phone number format"));
            }

            // Find user by phone number
            Optional<User> userOptional = userRepository.findByContactInformation(cleanPhone);
            if (!userOptional.isPresent()) {
                // Try with 12-digit format as backup
                String phoneWith63 = "63" + cleanPhone;
                userOptional = userRepository.findByContactInformation(phoneWith63);

                if (!userOptional.isPresent()) {
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("No account found with this phone number"));
                }
            }

            User user = userOptional.get();

            // Verify OTP - use same format as when OTP was sent (12-digit with country
            // code)
            // Use the format that matches the database storage
            String storedPhone = user.getContactInformation();
            String smsPhone;
            if (storedPhone.length() == 12 && storedPhone.startsWith("63")) {
                smsPhone = storedPhone; // Already in 12-digit format
            } else {
                smsPhone = "63" + storedPhone; // Add country code to 10-digit format
            }
            OTPVerificationResponse otpResult = otpService.verifyOTP(smsPhone, otpCode, user.getEmail());
            if (!otpResult.isSuccess()) {
                return ResponseEntity.badRequest().body(new MessageResponse(otpResult.getMessage()));
            }

            // SECURITY: Check if new password is the same as current password (even for
            // password reset)
            if (encoder.matches(newPassword, user.getPassword())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("New password must be different from your current password"));
            }

            // Update password in MongoDB
            user.setPassword(encoder.encode(newPassword));
            user.setUpdatedAt(new Date());
            userRepository.save(user);

            // SECURITY: Invalidate all existing tokens for this user after password reset
            // This forces logout on all devices for security purposes
            tokenService.revokeAllUserTokens(user.getId());

            // Clean up verified OTP after successful password reset
            otpService.cleanupVerifiedOTP(smsPhone, user.getEmail());

            return ResponseEntity
                    .ok(new MessageResponse("Password reset successfully. Please log in with your new password."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error resetting password: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-forgot-password-otp")
    public ResponseEntity<?> verifyForgotPasswordOTP(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");
            String otpCode = request.get("otpCode");

            if (phone == null || phone.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Phone number is required"));
            }

            if (otpCode == null || otpCode.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("OTP code is required"));
            }

            // Format phone to match database format (10 digits without country code)
            String cleanPhone = phone.replaceAll("\\D", ""); // Remove all non-digits
            if (cleanPhone.startsWith("63") && cleanPhone.length() == 12) {
                cleanPhone = cleanPhone.substring(2); // Remove '63' prefix
            } else if (cleanPhone.length() != 10) {
                return ResponseEntity.badRequest().body(new MessageResponse("Invalid phone number format"));
            }

            // Find user by phone number
            Optional<User> userOptional = userRepository.findByContactInformation(cleanPhone);
            if (!userOptional.isPresent()) {
                // Try with 12-digit format as backup
                String phoneWith63 = "63" + cleanPhone;
                userOptional = userRepository.findByContactInformation(phoneWith63);

                if (!userOptional.isPresent()) {
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("No account found with this phone number"));
                }
            }

            User user = userOptional.get();

            // Format phone for OTP verification - use same format as when OTP was sent
            String storedPhone = user.getContactInformation();
            String smsPhone;
            if (storedPhone.length() == 12 && storedPhone.startsWith("63")) {
                smsPhone = storedPhone; // Already in 12-digit format
            } else {
                smsPhone = "63" + storedPhone; // Add country code to 10-digit format
            }

            // Verify OTP but DO NOT delete it yet - we need it for password reset
            OTPVerificationResponse otpResult = otpService.verifyOTPWithoutDelete(smsPhone, otpCode, user.getEmail());
            if (!otpResult.isSuccess()) {
                return ResponseEntity.badRequest().body(new MessageResponse(otpResult.getMessage()));
            }

            return ResponseEntity
                    .ok(new MessageResponse("OTP verified successfully. You can now reset your password."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error verifying OTP: " + e.getMessage()));
        }
    }

}
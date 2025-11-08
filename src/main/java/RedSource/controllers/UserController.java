package RedSource.controllers;

import RedSource.entities.User;
import RedSource.entities.DTO.UserDTO;
import RedSource.entities.utils.MessageUtils;
import RedSource.entities.utils.ResponseUtils;
import RedSource.repositories.UserRepository;
import RedSource.services.UserService;
import RedSource.services.OTPService;
import RedSource.services.FileStorageService;
import RedSource.entities.DTO.auth.OTPVerificationResponse;
import RedSource.entities.enums.UserRoleType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@PreAuthorize("hasRole('DONOR') or hasRole('ADMIN')")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public static final String USER = "User";
    public static final String USERS = "Users";
    public static final String ROLE = "Role";

    private final UserService userService;
    private final UserRepository userRepository;
    private final OTPService otpService;
    private final FileStorageService fileStorageService;

    // Get all users
    @GetMapping
    public ResponseEntity<?> getAll() {
        logger.debug("GET /api/user - Retrieving all users");
        try {
            List<User> users = userService.getAll();
            List<UserDTO> userDTOs = users.stream()
                    .map(UserDTO::new)
                    .collect(Collectors.toList());
            logger.info("GET /api/user - Successfully retrieved {} users", users.size());
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(USERS),
                            userDTOs
                    )
            );
        } catch (Exception e) {
            logger.error("GET /api/user - Error retrieving users: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Get all users by a specific role (filter)
    @GetMapping("/filter")
    public ResponseEntity<?> getAllByFilter(@RequestParam(required = false) String role) {
        logger.debug("GET /api/user/filter - Filtering users by role: {}", role);
        if (role == null) {
            return getAll();
        }
        try {
            List<User> users = userService.getAllByFilter(role);
            logger.info("GET /api/user/filter - Successfully retrieved {} users with role: {}", users.size(), role);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(USERS),
                            users
                    )
            );
        } catch (Exception e) {
            logger.error("GET /api/user/filter - Error filtering users by role {}: {}", role, e.getMessage(), e);
            throw e;
        }
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        logger.debug("GET /api/user/{} - Retrieving user by ID", id);
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                logger.warn("GET /api/user/{} - User not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );
            }
            logger.info("GET /api/user/{} - Successfully retrieved user: {}", id, user.getEmail());
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(USER),
                            new UserDTO(user)
                    )
            );
        } catch (Exception e) {
            logger.error("GET /api/user/{} - Error retrieving user: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // Save a new user
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult) {
        logger.debug("POST /api/user - Creating new user: {}", userDTO.getEmail());
        if (bindingResult.hasErrors()) {
            logger.warn("POST /api/user - Validation errors: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            MessageUtils.validationErrors(bindingResult)
                    )
            );
        }
        try {
            User user = User.builder()
                    .name(userDTO.getName())
                    .email(userDTO.getEmail())
                    .role(userDTO.getRole())
                    .contactInformation(userDTO.getContactInformation())
                    .bloodType(userDTO.getBloodType())
                    .address(userDTO.getAddress())
                    .age(userDTO.getAge())
                    .sex(userDTO.getSex())
                    .dateOfBirth(userDTO.getDateOfBirth())
                    .build();

            User savedUser = userService.save(user);
            logger.info("POST /api/user - Successfully created user: {} (ID: {})", savedUser.getEmail(), savedUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.CREATED,
                            MessageUtils.saveSuccess(userDTO.getRole().name()),
                            new UserDTO(savedUser)
                    )
            );
        } catch (Exception e) {
            logger.error("POST /api/user - Error creating user: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Update an existing user
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Map<String, Object> requestBody) {
        logger.debug("PUT /api/user/{} - Updating user", id);
        try {
            // Extract UserDTO from request body
            UserDTO userDTO = mapToUserDTO(requestBody);
            
            // Handle phone number verification if OTP is provided
            String otpCode = (String) requestBody.get("otpCode");
            if (otpCode != null && !otpCode.trim().isEmpty()) {
                // Phone number change requires OTP verification
                String newPhoneNumber = userDTO.getContactInformation();
                if (newPhoneNumber == null || newPhoneNumber.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(
                            ResponseUtils.buildErrorResponse(
                                    HttpStatus.BAD_REQUEST,
                                    "Phone number is required for OTP verification"
                            )
                    );
                }
                
                // Get user's email for OTP verification
                Optional<User> existingUserOpt = userRepository.findById(id);
                if (!existingUserOpt.isPresent()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                            ResponseUtils.buildErrorResponse(
                                    HttpStatus.NOT_FOUND,
                                    "User not found"
                            )
                    );
                }
                
                User existingUser = existingUserOpt.get();
                String email = existingUser.getEmail();
                
                // Verify OTP for phone number change
                OTPVerificationResponse otpResult = otpService.verifyOTP(newPhoneNumber, otpCode, email);
                if (!otpResult.isSuccess()) {
                    return ResponseEntity.badRequest().body(
                            ResponseUtils.buildErrorResponse(
                                    HttpStatus.BAD_REQUEST,
                                    "Phone verification failed: " + otpResult.getMessage()
                            )
                    );
                }
            }
            
            // Check if username is already taken by another user
            if (userDTO.getUsername() != null) {
                Optional<User> existingUserWithUsername = userRepository.findByUsername(userDTO.getUsername());
                if (existingUserWithUsername.isPresent() && !existingUserWithUsername.get().getId().equals(id)) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                                    "Username is already taken!"
                    )
            );
        }
            }

            User user = User.builder()
                    .id(id)
                    .name(userDTO.getName())
                    .email(userDTO.getEmail())
                    .username(userDTO.getUsername())
                    .role(userDTO.getRole())
                    .contactInformation(userDTO.getContactInformation())
                    .bloodType(userDTO.getBloodType())
                    .address(userDTO.getAddress())
                    .age(userDTO.getAge())
                    .sex(userDTO.getSex())
                    .dateOfBirth(userDTO.getDateOfBirth())
                    .profilePhotoUrl(userDTO.getProfilePhotoUrl())
                    .build();

            User updatedUser = userService.update(id, user);
            logger.info("PUT /api/user/{} - Successfully updated user", id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.updateSuccess(userDTO.getRole().name()),
                            new UserDTO(updatedUser)
                    )
            );
        } catch (Exception e) {
            logger.error("PUT /api/user/{} - Error updating user: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.NOT_FOUND,
                            e.getMessage()
                    )
            );
        }
    }

    // Delete a user
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @RequestParam(required = false) String role) {
        logger.debug("DELETE /api/user/{} - Deleting user with role: {}", id, role != null ? role : USER);
        try {
            userService.delete(id, role != null ? role : USER);
            logger.info("DELETE /api/user/{} - Successfully deleted user", id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.deleteSuccess(role != null ? role : USER)
                    )
            );
        } catch (Exception e) {
            logger.error("DELETE /api/user/{} - Error deleting user: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.NOT_FOUND,
                            e.getMessage()
                    )
            );
        }
    }

    private UserDTO mapToUserDTO(Map<String, Object> requestBody) {
        UserDTO userDTO = new UserDTO();
        userDTO.setName((String) requestBody.get("name"));
        userDTO.setEmail((String) requestBody.get("email"));
        userDTO.setUsername((String) requestBody.get("username"));
        
        // Handle role conversion
        String roleStr = (String) requestBody.get("role");
        if (roleStr != null) {
            userDTO.setRole(UserRoleType.valueOf(roleStr));
        }
        
        userDTO.setContactInformation((String) requestBody.get("contactInformation"));
        userDTO.setBloodType((String) requestBody.get("bloodType"));
        userDTO.setAddress((String) requestBody.get("address"));
        userDTO.setAge((Integer) requestBody.get("age"));
        userDTO.setSex((String) requestBody.get("sex"));
        userDTO.setProfilePhotoUrl((String) requestBody.get("profilePhotoUrl"));
        
        // Handle date conversion
        String dateOfBirthStr = (String) requestBody.get("dateOfBirth");
        if (dateOfBirthStr != null && !dateOfBirthStr.trim().isEmpty()) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                userDTO.setDateOfBirth(formatter.parse(dateOfBirthStr));
            } catch (ParseException e) {
                try {
                    // Try alternative format
                    SimpleDateFormat altFormatter = new SimpleDateFormat("yyyy-MM-dd");
                    userDTO.setDateOfBirth(altFormatter.parse(dateOfBirthStr));
                } catch (ParseException e2) {
                    // If parsing fails, leave as null
                    userDTO.setDateOfBirth(null);
                }
            }
        }
        
        return userDTO;
    }

    // Upload profile photo
    @PostMapping(value = "/{id}/upload-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfilePhoto(
            @PathVariable String id,
            @RequestPart("photo") MultipartFile photo) {
        try {
            // Check if user exists
            Optional<User> userOpt = userRepository.findById(id);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );
            }

            User user = userOpt.get();
            
            // Delete old photo if exists
            if (user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isEmpty()) {
                try {
                    fileStorageService.deleteFile(user.getProfilePhotoUrl());
                } catch (Exception e) {
                    // Log the error but continue with the upload
                    logger.warn("Failed to delete old profile photo for user {}: {}", id, e.getMessage());
                }
            }
            
            // Store the new photo
            String photoUrl = fileStorageService.storeUserPhoto(photo);
            
            // Update user's profile photo URL
            user.setProfilePhotoUrl(photoUrl);
            user.setUpdatedAt(new Date());
            
            User updatedUser = userRepository.save(user);
            logger.info("POST /api/user/{}/upload-photo - Successfully uploaded profile photo", id);

            // Return response with photo URL
            Map<String, Object> response = new HashMap<>();
            response.put("profilePhotoUrl", photoUrl);
            response.put("user", new UserDTO(updatedUser));

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Profile photo uploaded successfully",
                            response
                    )
            );
        } catch (Exception e) {
            logger.error("POST /api/user/{}/upload-photo - Error uploading profile photo: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            "Failed to upload photo: " + e.getMessage()
                    )
            );
        }
    }

    // Get profile photo URL
    @GetMapping("/{id}/photo")
    public ResponseEntity<?> getProfilePhoto(@PathVariable String id) {
        logger.debug("GET /api/user/{}/photo - Retrieving profile photo URL", id);
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (!userOpt.isPresent()) {
                logger.warn("GET /api/user/{}/photo - User not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );
            }

            User user = userOpt.get();
            Map<String, String> response = new HashMap<>();
            response.put("profilePhotoUrl", user.getProfilePhotoUrl());
            logger.debug("GET /api/user/{}/photo - Successfully retrieved profile photo URL", id);

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Profile photo retrieved successfully",
                            response
                    )
            );
        } catch (Exception e) {
            logger.error("GET /api/user/{}/photo - Error retrieving profile photo: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to retrieve photo: " + e.getMessage()
                    )
            );
        }
    }

    // Remove profile photo
    @DeleteMapping("/{id}/photo")
    public ResponseEntity<?> removeProfilePhoto(@PathVariable String id) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );
            }

            User user = userOpt.get();
            
            // Delete the photo from storage if it exists
            if (user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isEmpty()) {
                try {
                    fileStorageService.deleteFile(user.getProfilePhotoUrl());
                } catch (Exception e) {
                    // Log the error but continue with the removal
                    logger.warn("Failed to delete profile photo from storage for user {}: {}", id, e.getMessage());
                }
            }
            
            // Remove the profile photo URL from the user
            user.setProfilePhotoUrl(null);
            user.setUpdatedAt(new Date());
            User updatedUser = userRepository.save(user);

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Profile photo removed successfully",
                            new UserDTO(updatedUser)
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to remove photo: " + e.getMessage()
                    )
            );
        }
    }

    // Archive user account
    @PutMapping("/{id}/archive")
    public ResponseEntity<?> archiveAccount(@PathVariable String id) {
        logger.debug("PUT /api/user/{}/archive - Archiving user account", id);
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                logger.warn("PUT /api/user/{}/archive - User not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );
            }

            // Set account status to archived
            user.setAccountStatus("ARCHIVED");
            user.setUpdatedAt(new Date());
            User updatedUser = userRepository.save(user);
            logger.info("PUT /api/user/{}/archive - Successfully archived user account", id);

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Account archived successfully",
                            new UserDTO(updatedUser)
                    )
            );
        } catch (Exception e) {
            logger.error("PUT /api/user/{}/archive - Error archiving account: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to archive account: " + e.getMessage()
                    )
            );
        }
    }

    // Activate user account
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateAccount(@PathVariable String id) {
        logger.debug("PUT /api/user/{}/activate - Activating user account", id);
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                logger.warn("PUT /api/user/{}/activate - User not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );
            }

            // Set account status to active
            user.setAccountStatus("ACTIVE");
            user.setUpdatedAt(new Date());
            User updatedUser = userRepository.save(user);
            logger.info("PUT /api/user/{}/activate - Successfully activated user account", id);

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Account activated successfully",
                            new UserDTO(updatedUser)
                    )
            );
        } catch (Exception e) {
            logger.error("PUT /api/user/{}/activate - Error activating account: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to activate account: " + e.getMessage()
                    )
            );
        }
    }
}

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

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@PreAuthorize("hasRole('DONOR') or hasRole('ADMIN')")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

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
        List<User> users = userService.getAll();
        List<UserDTO> userDTOs = users.stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.retrieveSuccess(USERS),
                        userDTOs
                )
        );
    }

    // Get all users by a specific role (filter)
    @GetMapping("/filter")
    public ResponseEntity<?> getAllByFilter(@RequestParam(required = false) String role) {
        if (role == null) {
            return getAll();
        }
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.retrieveSuccess(USERS),
                        userService.getAllByFilter(role)
                )
        );
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.NOT_FOUND,
                            "User not found"
                    )
            );
        }
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.retrieveSuccess(USER),
                        new UserDTO(user)
                )
        );
    }

    // Save a new user
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            MessageUtils.validationErrors(bindingResult)
                    )
            );
        }
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
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.saveSuccess(userDTO.getRole().name()),
                        new UserDTO(savedUser)
                )
        );
    }

    // Update an existing user
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Map<String, Object> requestBody) {
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
                    return ResponseEntity.badRequest().body(
                            ResponseUtils.buildErrorResponse(
                                    HttpStatus.BAD_REQUEST,
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
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.updateSuccess(userDTO.getRole().name()),
                            new UserDTO(updatedUser)
                    )
            );
        } catch (Exception e) {
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
        try {
            userService.delete(id, role != null ? role : USER);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.deleteSuccess(role != null ? role : USER)
                    )
            );
        } catch (Exception e) {
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
                    System.err.println("Failed to delete old profile photo: " + e.getMessage());
                }
            }
            
            // Store the new photo
            String photoUrl = fileStorageService.storeUserPhoto(photo);
            
            // Update user's profile photo URL
            user.setProfilePhotoUrl(photoUrl);
            user.setUpdatedAt(new Date());
            
            User updatedUser = userRepository.save(user);

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
            Map<String, String> response = new HashMap<>();
            response.put("profilePhotoUrl", user.getProfilePhotoUrl());

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Profile photo retrieved successfully",
                            response
                    )
            );
        } catch (Exception e) {
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
                    System.err.println("Failed to delete profile photo from storage: " + e.getMessage());
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
}

package RedSource.controllers;

import RedSource.entities.DTO.HospitalDTO;
import RedSource.entities.utils.MessageUtils;
import RedSource.entities.utils.ResponseUtils;
import RedSource.services.HospitalService;
import RedSource.services.OTPService;
import RedSource.services.FileStorageService;
import RedSource.entities.DTO.auth.OTPVerificationResponse;
import RedSource.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@PreAuthorize("hasRole('HOSPITAL') or hasRole('ADMIN')")
@RestController
@RequestMapping("/api/hospital")
@RequiredArgsConstructor
public class HospitalController {

    private static final Logger logger = LoggerFactory.getLogger(HospitalController.class);

    public static final String HOSPITAL = "Hospital";
    public static final String HOSPITALS = "Hospitals";

    private final HospitalService hospitalService;
    private final OTPService otpService;
    private final FileStorageService fileStorageService;

    // Get all hospitals
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAll() {
        logger.debug("GET /api/hospital - Retrieving all hospitals");
        try {
            List<HospitalDTO> hospitals = hospitalService.getAll();
            logger.info("GET /api/hospital - Successfully retrieved {} hospitals", hospitals.size());
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(HOSPITALS),
                            hospitals
                    )
            );
        } catch (Exception e) {
            logger.error("GET /api/hospital - Error retrieving hospitals: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Get hospital by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getHospitalById(@PathVariable String id) {
        logger.debug("GET /api/hospital/{} - Retrieving hospital by ID", id);
        try {
            HospitalDTO hospital = hospitalService.getById(id);
            if (hospital == null) {
                logger.warn("GET /api/hospital/{} - Hospital not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Hospital not found"
                        )
                );
            }
            logger.debug("GET /api/hospital/{} - Successfully retrieved hospital: {}", id, hospital.getHospitalName());
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(HOSPITAL),
                            hospital
                    )
            );
        } catch (Exception e) {
            logger.error("GET /api/hospital/{} - Error retrieving hospital: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // Get current hospital profile (for authenticated hospital)
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        logger.debug("GET /api/hospital/profile - Retrieving current hospital profile");
        try {
            if (userDetails == null) {
                logger.warn("GET /api/hospital/profile - Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.UNAUTHORIZED,
                                "Authentication required"
                        )
                );
            }
            
            // Get hospital by email from JWT token
            HospitalDTO hospital = hospitalService.getByEmail(userDetails.getEmail());
            if (hospital == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Hospital profile not found"
                        )
                );
            }
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(HOSPITAL),
                            hospital
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching hospital profile: " + e.getMessage()
                    )
            );
        }
    }

    // Create a new hospital
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> save(@Valid @RequestBody HospitalDTO hospitalDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            MessageUtils.validationErrors(bindingResult)
                    )
            );
        }

        // Check if email already exists
        if (hospitalService.existsByEmail(hospitalDTO.getEmail())) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            "Email already exists"
                    )
            );
        }

        // Check if username already exists
        if (hospitalDTO.getUsername() != null && hospitalService.existsByUsername(hospitalDTO.getUsername())) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            "Username already exists"
                    )
            );
        }

        logger.debug("POST /api/hospital - Creating new hospital: {}", hospitalDTO.getHospitalName());
        try {
            HospitalDTO savedHospital = hospitalService.save(hospitalDTO);
            logger.info("POST /api/hospital - Successfully created hospital: {} (ID: {})", savedHospital.getHospitalName(), savedHospital.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.CREATED,
                            MessageUtils.saveSuccess(HOSPITAL),
                            savedHospital
                    )
            );
        } catch (Exception e) {
            logger.error("POST /api/hospital - Error creating hospital: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Update hospital profile
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Map<String, Object> requestBody) {
        logger.debug("PUT /api/hospital/{} - Updating hospital profile", id);
        try {
            // Extract HospitalDTO from request body
            HospitalDTO hospitalDTO = mapToHospitalDTO(requestBody);
            
            // Handle phone number verification if OTP is provided
            String otpCode = (String) requestBody.get("otpCode");
            if (otpCode != null && !otpCode.trim().isEmpty()) {
                String newPhoneNumber = hospitalDTO.getPhone();
                if (newPhoneNumber == null || newPhoneNumber.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(
                            ResponseUtils.buildErrorResponse(
                                    HttpStatus.BAD_REQUEST,
                                    "Phone number is required for OTP verification"
                            )
                    );
                }
                
                // Get hospital's email for OTP verification
                HospitalDTO existingHospital = hospitalService.getById(id);
                if (existingHospital == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                            ResponseUtils.buildErrorResponse(
                                    HttpStatus.NOT_FOUND,
                                    "Hospital not found"
                            )
                    );
                }
                
                String email = existingHospital.getEmail();
                
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

            HospitalDTO updatedHospital = hospitalService.update(id, hospitalDTO);
            if (updatedHospital == null) {
                logger.warn("PUT /api/hospital/{} - Hospital not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Hospital not found"
                        )
                );
            }
            logger.info("PUT /api/hospital/{} - Successfully updated hospital: {}", id, updatedHospital.getHospitalName());
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.updateSuccess(HOSPITAL),
                            updatedHospital
                    )
            );
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            HttpStatus status = (message.contains("not found") || message.contains("does not exist")) 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(
                    ResponseUtils.buildErrorResponse(status, e.getMessage())
            );
        }
    }

    // Update hospital password
    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable String id, @RequestBody Map<String, String> requestBody) {
        logger.debug("PUT /api/hospital/{}/password - Updating hospital password", id);
        try {
            String currentPassword = requestBody.get("currentPassword");
            String newPassword = requestBody.get("newPassword");
            
            // Validate current password
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                "Current password is required"
                        )
                );
            }
            
            // Validate new password
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                "New password is required"
                        )
                );
            }

            // Validate password length
            if (newPassword.length() < 8) {
                return ResponseEntity.badRequest().body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                "New password must be at least 8 characters long"
                        )
                );
            }

            // Validate password strength (must contain uppercase, lowercase, number, and special character)
            if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
                return ResponseEntity.badRequest().body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character (@$!%*?&)"
                        )
                );
            }

            // Update password with current password validation
            HospitalDTO updatedHospital = hospitalService.updatePassword(id, currentPassword, newPassword);
            logger.info("PUT /api/hospital/{}/password - Successfully updated hospital password", id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Password updated successfully",
                            updatedHospital
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            e.getMessage()
                    )
            );
        }
    }

    // Send OTP for password change
    @PostMapping("/{id}/send-password-otp")
    public ResponseEntity<?> sendPasswordOTP(@PathVariable String id) {
        logger.debug("POST /api/hospital/{}/send-password-otp - Sending password OTP", id);
        try {
            HospitalDTO hospital = hospitalService.getById(id);
            if (hospital == null) {
                logger.warn("POST /api/hospital/{}/send-password-otp - Hospital not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Hospital not found"
                        )
                );
            }

            otpService.generateAndSendOTP(hospital.getPhone(), hospital.getEmail());
            logger.info("POST /api/hospital/{}/send-password-otp - Successfully sent OTP to hospital", id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "OTP sent successfully"
                    )
            );
        } catch (Exception e) {
            logger.error("POST /api/hospital/{}/send-password-otp - Error sending OTP: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            "Failed to send OTP: " + e.getMessage()
                    )
            );
        }
    }

    // Send OTP for phone verification
    @PostMapping("/{id}/send-phone-otp")
    public ResponseEntity<?> sendPhoneOTP(@PathVariable String id, @RequestBody Map<String, String> requestBody) {
        logger.debug("POST /api/hospital/{}/send-phone-otp - Sending phone OTP", id);
        try {
            String newPhoneNumber = requestBody.get("phoneNumber");
            if (newPhoneNumber == null || newPhoneNumber.trim().isEmpty()) {
                logger.warn("POST /api/hospital/{}/send-phone-otp - Phone number is required", id);
                return ResponseEntity.badRequest().body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                "Phone number is required"
                        )
                );
            }

            HospitalDTO hospital = hospitalService.getById(id);
            if (hospital == null) {
                logger.warn("POST /api/hospital/{}/send-phone-otp - Hospital not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Hospital not found"
                        )
                );
            }

            otpService.generateAndSendOTP(newPhoneNumber, hospital.getEmail());
            logger.info("POST /api/hospital/{}/send-phone-otp - Successfully sent phone OTP", id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "OTP sent successfully"
                    )
            );
        } catch (Exception e) {
            logger.error("POST /api/hospital/{}/send-phone-otp - Error sending OTP: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            "Failed to send OTP: " + e.getMessage()
                    )
            );
        }
    }

    // Upload profile photo (Admin only)
    @PostMapping(value = "/{id}/upload-profile-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadProfilePhoto(
            @PathVariable String id,
            @RequestPart("photo") MultipartFile photo) {
        logger.debug("POST /api/hospital/{}/upload-profile-photo - Uploading profile photo (size: {} bytes)", id, photo.getSize());
        try {
            HospitalDTO hospital = hospitalService.getById(id);
            if (hospital == null) {
                logger.warn("POST /api/hospital/{}/upload-profile-photo - Hospital not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Hospital not found"
                        )
                );
            }
            
            // Delete old photo if exists
            if (hospital.getProfilePhotoUrl() != null && !hospital.getProfilePhotoUrl().isEmpty()) {
                try {
                    fileStorageService.deleteFile(hospital.getProfilePhotoUrl());
                    logger.debug("POST /api/hospital/{}/upload-profile-photo - Deleted old profile photo", id);
                } catch (Exception e) {
                    logger.warn("POST /api/hospital/{}/upload-profile-photo - Failed to delete old profile photo: {}", id, e.getMessage());
                }
            }
            
            // Store the new photo
            String photoUrl = fileStorageService.storeUserPhoto(photo);
            
            // Update hospital's profile photo URL
            HospitalDTO updatedHospital = hospitalService.updateProfilePhoto(id, photoUrl);
            logger.info("POST /api/hospital/{}/upload-profile-photo - Successfully uploaded profile photo", id);

            // Return response with photo URL
            Map<String, Object> response = new HashMap<>();
            response.put("profilePhotoUrl", photoUrl);
            response.put("hospital", updatedHospital);

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Profile photo uploaded successfully",
                            response
                    )
            );
        } catch (Exception e) {
            logger.error("POST /api/hospital/{}/upload-profile-photo - Error uploading photo: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            "Failed to upload photo: " + e.getMessage()
                    )
            );
        }
    }

    // Remove profile photo (Admin only)
    @DeleteMapping("/{id}/photo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeProfilePhoto(@PathVariable String id) {
        logger.debug("DELETE /api/hospital/{}/photo - Removing profile photo", id);
        try {
            HospitalDTO hospital = hospitalService.getById(id);
            if (hospital == null) {
                logger.warn("DELETE /api/hospital/{}/photo - Hospital not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Hospital not found"
                        )
                );
            }
            
            // Delete the photo from storage if it exists
            if (hospital.getProfilePhotoUrl() != null && !hospital.getProfilePhotoUrl().isEmpty()) {
                try {
                    fileStorageService.deleteFile(hospital.getProfilePhotoUrl());
                    logger.debug("DELETE /api/hospital/{}/photo - Deleted photo from storage", id);
                } catch (Exception e) {
                    logger.warn("DELETE /api/hospital/{}/photo - Failed to delete profile photo from storage: {}", id, e.getMessage());
                }
            }
            
            // Remove the profile photo URL from the hospital
            HospitalDTO updatedHospital = hospitalService.updateProfilePhoto(id, null);
            logger.info("DELETE /api/hospital/{}/photo - Successfully removed profile photo", id);

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Profile photo removed successfully",
                            updatedHospital
                    )
            );
        } catch (Exception e) {
            logger.error("DELETE /api/hospital/{}/photo - Error removing photo: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to remove photo: " + e.getMessage()
                    )
            );
        }
    }

    // Delete hospital
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            hospitalService.delete(id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.deleteSuccess(HOSPITAL)
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

    private HospitalDTO mapToHospitalDTO(Map<String, Object> requestBody) {
        HospitalDTO hospitalDTO = new HospitalDTO();
        hospitalDTO.setHospitalName((String) requestBody.get("hospitalName"));
        hospitalDTO.setEmail((String) requestBody.get("email"));
        hospitalDTO.setUsername((String) requestBody.get("username"));
        hospitalDTO.setPhone((String) requestBody.get("phone"));
        hospitalDTO.setAddress((String) requestBody.get("address"));
        hospitalDTO.setHospitalId((String) requestBody.get("hospitalId"));
        hospitalDTO.setLicenseNumber((String) requestBody.get("licenseNumber"));
        hospitalDTO.setProfilePhotoUrl((String) requestBody.get("profilePhotoUrl"));
        
        return hospitalDTO;
    }
}

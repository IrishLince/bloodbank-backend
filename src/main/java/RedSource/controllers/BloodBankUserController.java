package RedSource.controllers;

import RedSource.entities.BloodBankUser;
import RedSource.entities.utils.MessageUtils;
import RedSource.entities.utils.ResponseUtils;
import RedSource.services.BloodBankUserService;
import RedSource.services.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/bloodbank-users")
@RequiredArgsConstructor
public class BloodBankUserController {

    public static final String BLOOD_BANK_USER = "Blood Bank User";
    public static final String BLOOD_BANK_USERS = "Blood Bank Users";

    private final BloodBankUserService bloodBankUserService;
    
    @Autowired
    private FileStorageService fileStorageService;

    // Get all blood bank users (for Select Blood Source)
    @GetMapping
    @PreAuthorize("hasRole('HOSPITAL') or hasRole('ADMIN') or hasRole('DONOR')")
    public ResponseEntity<?> getAll() {
        try {
            List<BloodBankUser> bloodBankUsers = bloodBankUserService.getAll();
            
            // Transform to match frontend expectations
            List<Map<String, Object>> bloodSources = bloodBankUsers.stream()
                .map(bloodBankUser -> {
                    Map<String, Object> source = new HashMap<>();
                    source.put("id", bloodBankUser.getId());
                    source.put("name", bloodBankUser.getBloodBankName());
                    source.put("location", bloodBankUser.getAddress()); // Frontend expects 'location'
                    source.put("phone", bloodBankUser.getPhone());
                    source.put("email", bloodBankUser.getEmail());
                    source.put("operatingHours", bloodBankUser.getOperatingHours());
                    source.put("bloodBankId", bloodBankUser.getBloodBankId());
                    source.put("profilePhotoUrl", bloodBankUser.getProfilePhotoUrl());
                    source.put("coverImageUrl", bloodBankUser.getCoverImageUrl());
                    
                    // Add mock inventory for now (since BloodBankUser doesn't have inventory)
                    // TODO: Link to actual inventory from BloodBank entity
                    List<Map<String, Object>> inventory = List.of(
                        Map.of("bloodType", "A+", "units", 50),
                        Map.of("bloodType", "A-", "units", 30),
                        Map.of("bloodType", "B+", "units", 45),
                        Map.of("bloodType", "B-", "units", 25),
                        Map.of("bloodType", "AB+", "units", 20),
                        Map.of("bloodType", "AB-", "units", 15),
                        Map.of("bloodType", "O+", "units", 60),
                        Map.of("bloodType", "O-", "units", 40)
                    );
                    source.put("inventory", inventory);
                    
                    return source;
                })
                .toList();
                
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(BLOOD_BANK_USERS),
                            bloodSources
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching blood bank users: " + e.getMessage()
                    )
            );
        }
    }

    // Get blood bank user by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('BLOODBANK') or hasRole('ADMIN') or hasRole('HOSPITAL')")
    public ResponseEntity<?> getById(@PathVariable String id) {
        try {
            BloodBankUser bloodBankUser = bloodBankUserService.getById(id);
            if (bloodBankUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Blood bank user not found"
                        )
                );
            }
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(BLOOD_BANK_USER),
                            bloodBankUser
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching blood bank user: " + e.getMessage()
                    )
            );
        }
    }

    // Update blood bank user profile
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BLOODBANK') or hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody BloodBankUser bloodBankUser, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            "Validation errors: " + result.getAllErrors()
                    )
            );
        }

        try {
            BloodBankUser updatedBloodBankUser = bloodBankUserService.update(id, bloodBankUser);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.updateSuccess(BLOOD_BANK_USER),
                            updatedBloodBankUser
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error updating blood bank user: " + e.getMessage()
                    )
            );
        }
    }

    // Delete blood bank user
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            bloodBankUserService.deleteById(id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.deleteSuccess(BLOOD_BANK_USER),
                            null
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error deleting blood bank user: " + e.getMessage()
                    )
            );
        }
    }

    // Update preferred blood types
    @PutMapping("/{id}/preferred-bloodtypes")
    @PreAuthorize("hasRole('BLOODBANK') or hasRole('ADMIN')")
    public ResponseEntity<?> updatePreferredBloodTypes(@PathVariable String id, @RequestBody Map<String, List<String>> request) {
        try {
            List<String> preferredBloodTypes = request.get("preferredBloodTypes");
            if (preferredBloodTypes == null) {
                return ResponseEntity.badRequest().body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                "Missing preferredBloodTypes in request body"
                        )
                );
            }

            BloodBankUser updatedBloodBankUser = bloodBankUserService.updatePreferredBloodTypes(id, preferredBloodTypes);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Preferred blood types updated successfully",
                            updatedBloodBankUser
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error updating preferred blood types: " + e.getMessage()
                    )
            );
        }
    }

    // Upload profile photo
    @PostMapping(value = "/{id}/upload-profile-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('BLOODBANK') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadProfilePhoto(
            @PathVariable String id,
            @RequestParam("photo") MultipartFile photo) {
        try {
            // Find blood bank user
            BloodBankUser bloodBankUser = bloodBankUserService.getById(id);
            if (bloodBankUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Blood bank user not found"
                        )
                );
            }

            // Delete old profile photo if it exists
            if (bloodBankUser.getProfilePhotoUrl() != null && !bloodBankUser.getProfilePhotoUrl().isEmpty()) {
                try {
                    fileStorageService.deleteFile(bloodBankUser.getProfilePhotoUrl());
                } catch (Exception e) {
                    System.err.println("Failed to delete old profile photo: " + e.getMessage());
                }
            }
            
            // Store the new photo
            String photoUrl = fileStorageService.storeUserPhoto(photo);
            
            // Update blood bank user's profile photo URL
            bloodBankUser.setProfilePhotoUrl(photoUrl);
            bloodBankUser.setUpdatedAt(new Date());
            BloodBankUser updatedBloodBankUser = bloodBankUserService.update(id, bloodBankUser);

            // Return response with photo URL
            Map<String, Object> response = new HashMap<>();
            response.put("profilePhotoUrl", photoUrl);
            response.put("bloodBankUser", updatedBloodBankUser);

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Profile photo uploaded successfully",
                            response
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error uploading profile photo: " + e.getMessage()
                    )
            );
        }
    }

    // Remove profile photo
    @DeleteMapping("/{id}/profile-photo")
    @PreAuthorize("hasRole('BLOODBANK') or hasRole('ADMIN')")
    public ResponseEntity<?> removeProfilePhoto(@PathVariable String id) {
        try {
            // Find blood bank user
            BloodBankUser bloodBankUser = bloodBankUserService.getById(id);
            if (bloodBankUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Blood bank user not found"
                        )
                );
            }

            // Delete the profile photo from storage if it exists
            if (bloodBankUser.getProfilePhotoUrl() != null && !bloodBankUser.getProfilePhotoUrl().isEmpty()) {
                try {
                    fileStorageService.deleteFile(bloodBankUser.getProfilePhotoUrl());
                } catch (Exception e) {
                    System.err.println("Failed to delete profile photo from storage: " + e.getMessage());
                }
            }
            
            // Remove the profile photo URL from the blood bank user
            bloodBankUser.setProfilePhotoUrl(null);
            bloodBankUser.setUpdatedAt(new Date());
            BloodBankUser updatedBloodBankUser = bloodBankUserService.update(id, bloodBankUser);

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Profile photo removed successfully",
                            updatedBloodBankUser
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error removing profile photo: " + e.getMessage()
                    )
            );
        }
    }

    // Upload cover image
    @PostMapping(value = "/{id}/upload-cover-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('BLOODBANK') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadCoverImage(
            @PathVariable String id,
            @RequestParam("coverImage") MultipartFile coverImage) {
        try {
            // Find blood bank user
            BloodBankUser bloodBankUser = bloodBankUserService.getById(id);
            if (bloodBankUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Blood bank user not found"
                        )
                );
            }

            // Delete old cover image if it exists
            if (bloodBankUser.getCoverImageUrl() != null && !bloodBankUser.getCoverImageUrl().isEmpty()) {
                try {
                    fileStorageService.deleteFile(bloodBankUser.getCoverImageUrl());
                } catch (Exception e) {
                    System.err.println("Failed to delete old cover image: " + e.getMessage());
                }
            }
            
            // Store the new cover image
            String coverImageUrl = fileStorageService.storeUserPhoto(coverImage);
            
            // Update blood bank user's cover image URL
            bloodBankUser.setCoverImageUrl(coverImageUrl);
            bloodBankUser.setUpdatedAt(new Date());
            BloodBankUser updatedBloodBankUser = bloodBankUserService.update(id, bloodBankUser);

            // Return response with cover image URL
            Map<String, Object> response = new HashMap<>();
            response.put("coverImageUrl", coverImageUrl);
            response.put("bloodBankUser", updatedBloodBankUser);

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Cover image uploaded successfully",
                            response
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error uploading cover image: " + e.getMessage()
                    )
            );
        }
    }

    // Remove cover image
    @DeleteMapping("/{id}/cover-image")
    @PreAuthorize("hasRole('BLOODBANK') or hasRole('ADMIN')")
    public ResponseEntity<?> removeCoverImage(@PathVariable String id) {
        try {
            // Find blood bank user
            BloodBankUser bloodBankUser = bloodBankUserService.getById(id);
            if (bloodBankUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Blood bank user not found"
                        )
                );
            }

            // Delete the cover image from storage if it exists
            if (bloodBankUser.getCoverImageUrl() != null && !bloodBankUser.getCoverImageUrl().isEmpty()) {
                try {
                    fileStorageService.deleteFile(bloodBankUser.getCoverImageUrl());
                } catch (Exception e) {
                    System.err.println("Failed to delete cover image from storage: " + e.getMessage());
                }
            }
            
            // Remove the cover image URL from the blood bank user
            bloodBankUser.setCoverImageUrl(null);
            bloodBankUser.setUpdatedAt(new Date());
            BloodBankUser updatedBloodBankUser = bloodBankUserService.update(id, bloodBankUser);

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Cover image removed successfully",
                            updatedBloodBankUser
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error removing cover image: " + e.getMessage()
                    )
            );
        }
    }
}

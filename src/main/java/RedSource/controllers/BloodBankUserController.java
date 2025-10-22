package RedSource.controllers;

import RedSource.entities.BloodBankUser;
import RedSource.entities.utils.MessageUtils;
import RedSource.entities.utils.ResponseUtils;
import RedSource.services.BloodBankUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
}

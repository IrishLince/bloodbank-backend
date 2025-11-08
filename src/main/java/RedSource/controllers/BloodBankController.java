package RedSource.controllers;

import RedSource.entities.BloodBankUser;
import RedSource.entities.utils.MessageUtils;
import RedSource.entities.utils.ResponseUtils;
import RedSource.services.BloodBankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PreAuthorize("hasRole('BLOODBANK') or hasRole('ADMIN') or hasRole('HOSPITAL')")
@RestController
@RequestMapping("/api/bloodbanks")
@RequiredArgsConstructor
public class BloodBankController {

    private static final Logger logger = LoggerFactory.getLogger(BloodBankController.class);

    public static final String BLOOD_BANK = "Blood Bank";
    public static final String BLOOD_BANKS = "Blood Banks";

    private final BloodBankService bloodBankService;

    // Get all blood banks
    @GetMapping
    public ResponseEntity<?> getAll() {
        logger.debug("GET /api/bloodbanks - Retrieving all blood banks");
        try {
            var bloodBanks = bloodBankService.getAll();
            logger.info("GET /api/bloodbanks - Successfully retrieved {} blood banks", bloodBanks.size());
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(BLOOD_BANKS),
                            bloodBanks));
        } catch (Exception e) {
            logger.error("GET /api/bloodbanks - Error retrieving blood banks: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Get blood bank by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        logger.debug("GET /api/bloodbanks/{} - Retrieving blood bank by ID", id);
        try {
            BloodBankUser bloodBank = bloodBankService.getById(id);
            if (bloodBank != null) {
                logger.info("GET /api/bloodbanks/{} - Successfully retrieved blood bank: {}", id, bloodBank.getBloodBankName());
                return ResponseEntity.ok(
                        ResponseUtils.buildSuccessResponse(
                                HttpStatus.OK,
                                MessageUtils.retrieveSuccess(BLOOD_BANK),
                                bloodBank));
            } else {
                logger.warn("GET /api/bloodbanks/{} - Blood bank not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                MessageUtils.retrieveError(BLOOD_BANK)));
            }
        } catch (Exception e) {
            logger.error("GET /api/bloodbanks/{} - Error retrieving blood bank: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // Create new blood bank
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody BloodBankUser bloodBank, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            "Validation errors: " + result.getAllErrors()));
        }

        logger.debug("POST /api/bloodbanks - Creating new blood bank: {}", bloodBank.getBloodBankName());
        try {
            BloodBankUser savedBloodBank = bloodBankService.save(bloodBank);
            logger.info("POST /api/bloodbanks - Successfully created blood bank: {} (ID: {})", savedBloodBank.getBloodBankName(), savedBloodBank.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.CREATED,
                            MessageUtils.saveSuccess(BLOOD_BANK),
                            savedBloodBank));
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            if (message.contains("not found") || message.contains("does not exist")) {
                status = HttpStatus.NOT_FOUND;
                logger.warn("POST /api/bloodbanks - Blood bank creation failed (not found): {}", e.getMessage());
            } else if (message.contains("already exists") || message.contains("duplicate")) {
                status = HttpStatus.CONFLICT;
                logger.warn("POST /api/bloodbanks - Blood bank creation failed (conflict): {}", e.getMessage());
            } else {
                logger.error("POST /api/bloodbanks - Error creating blood bank: {}", e.getMessage(), e);
            }
            return ResponseEntity.status(status).body(
                    ResponseUtils.buildErrorResponse(status, e.getMessage()));
        }
    }

    // Update blood bank
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody BloodBankUser bloodBank,
            BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            "Validation errors: " + result.getAllErrors()));
        }

        logger.debug("PUT /api/bloodbanks/{} - Updating blood bank", id);
        try {
            BloodBankUser updatedBloodBank = bloodBankService.update(id, bloodBank);
            if (updatedBloodBank == null) {
                logger.warn("PUT /api/bloodbanks/{} - Blood bank not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Blood bank not found"));
            }
            logger.info("PUT /api/bloodbanks/{} - Successfully updated blood bank: {}", id, updatedBloodBank.getBloodBankName());
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.updateSuccess(BLOOD_BANK),
                            updatedBloodBank));
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            HttpStatus status = (message.contains("not found") || message.contains("does not exist")) 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            if (status == HttpStatus.NOT_FOUND) {
                logger.warn("PUT /api/bloodbanks/{} - Update failed (not found): {}", id, e.getMessage());
            } else {
                logger.error("PUT /api/bloodbanks/{} - Error updating blood bank: {}", id, e.getMessage(), e);
            }
            return ResponseEntity.status(status).body(
                    ResponseUtils.buildErrorResponse(status, e.getMessage()));
        }
    }

    // Delete blood bank
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        logger.debug("DELETE /api/bloodbanks/{} - Deleting blood bank", id);
        try {
            bloodBankService.delete(id);
            logger.info("DELETE /api/bloodbanks/{} - Successfully deleted blood bank", id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.deleteSuccess(BLOOD_BANK),
                            null));
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            HttpStatus status = (message.contains("not found") || message.contains("does not exist")) 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            if (status == HttpStatus.NOT_FOUND) {
                logger.warn("DELETE /api/bloodbanks/{} - Delete failed (not found): {}", id, e.getMessage());
            } else {
                logger.error("DELETE /api/bloodbanks/{} - Error deleting blood bank: {}", id, e.getMessage(), e);
            }
            return ResponseEntity.status(status).body(
                    ResponseUtils.buildErrorResponse(status, e.getMessage()));
        }
    }
}

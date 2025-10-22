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

@PreAuthorize("hasRole('BLOODBANK') or hasRole('ADMIN') or hasRole('HOSPITAL')")
@RestController
@RequestMapping("/api/bloodbanks")
@RequiredArgsConstructor
public class BloodBankController {

    public static final String BLOOD_BANK = "Blood Bank";
    public static final String BLOOD_BANKS = "Blood Banks";

    private final BloodBankService bloodBankService;

    // Get all blood banks
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.retrieveSuccess(BLOOD_BANKS),
                        bloodBankService.getAll()));
    }

    // Get blood bank by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        BloodBankUser bloodBank = bloodBankService.getById(id);
        if (bloodBank != null) {
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(BLOOD_BANK),
                            bloodBank));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.NOT_FOUND,
                            MessageUtils.retrieveError(BLOOD_BANK)));
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

        try {
            BloodBankUser savedBloodBank = bloodBankService.save(bloodBank);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.CREATED,
                            MessageUtils.saveSuccess(BLOOD_BANK),
                            savedBloodBank));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error creating blood bank: " + e.getMessage()));
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

        try {
            BloodBankUser updatedBloodBank = bloodBankService.update(id, bloodBank);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.updateSuccess(BLOOD_BANK),
                            updatedBloodBank));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error updating blood bank: " + e.getMessage()));
        }
    }

    // Delete blood bank
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            bloodBankService.delete(id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.deleteSuccess(BLOOD_BANK),
                            null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error deleting blood bank: " + e.getMessage()));
        }
    }
}

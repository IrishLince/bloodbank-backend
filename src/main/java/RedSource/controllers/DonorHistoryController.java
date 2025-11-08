package RedSource.controllers;

import RedSource.entities.DonorHistory;
import RedSource.entities.utils.MessageUtils;
import RedSource.entities.utils.ResponseUtils;
import RedSource.services.DonorHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PreAuthorize("hasRole('DONOR') or hasRole('ADMIN')")
@RestController
@RequestMapping("/api/donor-history")
@RequiredArgsConstructor
public class DonorHistoryController {

    private static final Logger logger = LoggerFactory.getLogger(DonorHistoryController.class);

    public static final String DONOR_HISTORY = "Donor History";
    public static final String DONOR_HISTORIES = "Donor Histories";

    private final DonorHistoryService donorHistoryService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        logger.debug("GET /api/donor-history - Retrieving all donor histories");
        try {
            var histories = donorHistoryService.getAll();
            logger.info("GET /api/donor-history - Successfully retrieved {} donor histories", histories.size());
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(DONOR_HISTORIES),
                            histories
                    )
            );
        } catch (Exception e) {
            logger.error("GET /api/donor-history - Error retrieving donor histories: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        logger.debug("GET /api/donor-history/{} - Retrieving donor history by ID", id);
        try {
            DonorHistory donorHistory = donorHistoryService.getById(id);
            if (donorHistory == null) {
                logger.warn("GET /api/donor-history/{} - Donor history not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Donor history not found"
                        )
                );
            }
            logger.info("GET /api/donor-history/{} - Successfully retrieved donor history", id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(DONOR_HISTORY),
                            donorHistory
                    )
            );
        } catch (Exception e) {
            logger.error("GET /api/donor-history/{} - Error retrieving donor history: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody DonorHistory donorHistory, BindingResult bindingResult) {
        logger.debug("POST /api/donor-history - Creating new donor history");
        if (bindingResult.hasErrors()) {
            logger.warn("POST /api/donor-history - Validation errors: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            MessageUtils.validationErrors(bindingResult)
                    )
            );
        }
        try {
            DonorHistory savedDonorHistory = donorHistoryService.save(donorHistory);
            logger.info("POST /api/donor-history - Successfully created donor history (ID: {})", savedDonorHistory.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.CREATED,
                            MessageUtils.saveSuccess(DONOR_HISTORY),
                            savedDonorHistory
                    )
            );
        } catch (Exception e) {
            logger.error("POST /api/donor-history - Error creating donor history: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody DonorHistory donorHistory, BindingResult bindingResult) {
        logger.debug("PUT /api/donor-history/{} - Updating donor history", id);
        if (bindingResult.hasErrors()) {
            logger.warn("PUT /api/donor-history/{} - Validation errors: {}", id, bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            MessageUtils.validationErrors(bindingResult)
                    )
            );
        }
        try {
            DonorHistory updatedDonorHistory = donorHistoryService.update(id, donorHistory);
            if (updatedDonorHistory == null) {
                logger.warn("PUT /api/donor-history/{} - Donor history not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Donor history not found"
                        )
                );
            }
            logger.info("PUT /api/donor-history/{} - Successfully updated donor history", id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.updateSuccess(DONOR_HISTORY),
                            updatedDonorHistory
                    )
            );
        } catch (Exception e) {
            logger.error("PUT /api/donor-history/{} - Error updating donor history: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.NOT_FOUND,
                            e.getMessage()
                    )
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        logger.debug("DELETE /api/donor-history/{} - Deleting donor history", id);
        try {
            donorHistoryService.delete(id);
            logger.info("DELETE /api/donor-history/{} - Successfully deleted donor history", id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.deleteSuccess(DONOR_HISTORY)
                    )
            );
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            HttpStatus status = (message.contains("not found") || message.contains("does not exist")) 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            if (status == HttpStatus.NOT_FOUND) {
                logger.warn("DELETE /api/donor-history/{} - Delete failed (not found): {}", id, e.getMessage());
            } else {
                logger.error("DELETE /api/donor-history/{} - Error deleting donor history: {}", id, e.getMessage(), e);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.NOT_FOUND,
                            e.getMessage()
                    )
            );
        }
    }
} 
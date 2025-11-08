package RedSource.controllers;

import RedSource.entities.Fulfillment;
import RedSource.entities.utils.MessageUtils;
import RedSource.entities.utils.ResponseUtils;
import RedSource.services.FulfillmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/fulfillments")
@RequiredArgsConstructor
public class FulfillmentController {

    private static final Logger logger = LoggerFactory.getLogger(FulfillmentController.class);

    public static final String FULFILLMENT = "Fulfillment";
    public static final String FULFILLMENTS = "Fulfillments";

    private final FulfillmentService fulfillmentService;

    // Get all fulfillments
    @GetMapping
    public ResponseEntity<?> getAll() {
        logger.debug("GET /api/fulfillments - Retrieving all fulfillments");
        try {
            var fulfillments = fulfillmentService.getAll();
            logger.info("GET /api/fulfillments - Successfully retrieved {} fulfillments", fulfillments.size());
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(FULFILLMENTS),
                            fulfillments
                    )
            );
        } catch (Exception e) {
            logger.error("GET /api/fulfillments - Error retrieving fulfillments: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Get fulfillment by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        logger.debug("GET /api/fulfillments/{} - Retrieving fulfillment by ID", id);
        try {
            Fulfillment fulfillment = fulfillmentService.getById(id);
            if (fulfillment == null) {
                logger.warn("GET /api/fulfillments/{} - Fulfillment not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Fulfillment not found"
                        )
                );
            }
            logger.info("GET /api/fulfillments/{} - Successfully retrieved fulfillment", id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(FULFILLMENT),
                            fulfillment
                    )
            );
        } catch (Exception e) {
            logger.error("GET /api/fulfillments/{} - Error retrieving fulfillment: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // Save a new fulfillment
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody Fulfillment fulfillment, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            MessageUtils.validationErrors(bindingResult)
                    )
            );
        }
        logger.debug("POST /api/fulfillments - Creating new fulfillment");
        try {
            Fulfillment savedFulfillment = fulfillmentService.save(fulfillment);
            logger.info("POST /api/fulfillments - Successfully created fulfillment (ID: {})", savedFulfillment.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.CREATED,
                            MessageUtils.saveSuccess(FULFILLMENT),
                            savedFulfillment
                    )
            );
        } catch (Exception e) {
            logger.error("POST /api/fulfillments - Error creating fulfillment: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Update an existing fulfillment
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody Fulfillment fulfillment, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            MessageUtils.validationErrors(bindingResult)
                    )
            );
        }
        logger.debug("PUT /api/fulfillments/{} - Updating fulfillment", id);
        try {
            Fulfillment updatedFulfillment = fulfillmentService.update(id, fulfillment);
            if (updatedFulfillment == null) {
                logger.warn("PUT /api/fulfillments/{} - Fulfillment not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Fulfillment not found"
                        )
                );
            }
            logger.info("PUT /api/fulfillments/{} - Successfully updated fulfillment", id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.updateSuccess(FULFILLMENT),
                            updatedFulfillment
                    )
            );
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            HttpStatus status = (message.contains("not found") || message.contains("does not exist")) 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            if (status == HttpStatus.NOT_FOUND) {
                logger.warn("PUT /api/fulfillments/{} - Update failed (not found): {}", id, e.getMessage());
            } else {
                logger.error("PUT /api/fulfillments/{} - Error updating fulfillment: {}", id, e.getMessage(), e);
            }
            return ResponseEntity.status(status).body(
                    ResponseUtils.buildErrorResponse(status, e.getMessage())
            );
        }
    }

    // Delete a fulfillment
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        logger.debug("DELETE /api/fulfillments/{} - Deleting fulfillment", id);
        try {
            fulfillmentService.delete(id);
            logger.info("DELETE /api/fulfillments/{} - Successfully deleted fulfillment", id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.deleteSuccess(FULFILLMENT)
                    )
            );
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            HttpStatus status = (message.contains("not found") || message.contains("does not exist")) 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            if (status == HttpStatus.NOT_FOUND) {
                logger.warn("DELETE /api/fulfillments/{} - Delete failed (not found): {}", id, e.getMessage());
            } else {
                logger.error("DELETE /api/fulfillments/{} - Error deleting fulfillment: {}", id, e.getMessage(), e);
            }
            return ResponseEntity.status(status).body(
                    ResponseUtils.buildErrorResponse(status, e.getMessage())
            );
        }
    }
}

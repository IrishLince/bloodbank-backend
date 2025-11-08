package RedSource.controllers;

import RedSource.entities.VitalSigns;
import RedSource.entities.utils.MessageUtils;
import RedSource.entities.utils.ResponseUtils;
import RedSource.exceptions.ServiceException;
import RedSource.services.VitalSignsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/vital-signs")
@RequiredArgsConstructor
public class VitalSignsController {

    private static final Logger logger = LoggerFactory.getLogger(VitalSignsController.class);

    public static final String VITAL_SIGNS = "Vital Signs";
    public static final String VITAL_SIGNS_PLURAL = "Vital Signs";

    private final VitalSignsService vitalSignsService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        logger.debug("GET /api/vital-signs - Retrieving all vital signs");
        try {
            List<VitalSigns> vitalSigns = vitalSignsService.getAll();
            logger.info("GET /api/vital-signs - Successfully retrieved {} vital signs records", vitalSigns.size());
            return ResponseEntity.ok(vitalSigns);
        } catch (ServiceException e) {
            logger.error("GET /api/vital-signs - Error retrieving vital signs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        logger.debug("GET /api/vital-signs/{} - Retrieving vital signs by ID", id);
        try {
            VitalSigns vitalSigns = vitalSignsService.getById(id);
            if (vitalSigns == null) {
                logger.warn("GET /api/vital-signs/{} - Vital signs not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, "Vital signs not found")
                );
            }
            logger.info("GET /api/vital-signs/{} - Successfully retrieved vital signs", id);
            return ResponseEntity.ok(vitalSigns);
        } catch (ServiceException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("not found") || message.contains("does not exist")) {
                logger.warn("GET /api/vital-signs/{} - Vital signs not found: {}", id, e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage())
                );
            }
            logger.error("GET /api/vital-signs/{} - Error retrieving vital signs: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody VitalSigns vitalSigns) {
        logger.debug("POST /api/vital-signs - Creating new vital signs record");
        try {
            VitalSigns savedVitalSigns = vitalSignsService.save(vitalSigns);
            logger.info("POST /api/vital-signs - Successfully created vital signs record (ID: {})", savedVitalSigns.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedVitalSigns);
        } catch (ServiceException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("not found") || message.contains("does not exist")) {
                logger.warn("POST /api/vital-signs - Creation failed (not found): {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage())
                );
            }
            logger.error("POST /api/vital-signs - Error creating vital signs record: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody VitalSigns vitalSigns) {
        logger.debug("PUT /api/vital-signs/{} - Updating vital signs", id);
        try {
            VitalSigns updatedVitalSigns = vitalSignsService.update(id, vitalSigns);
            if (updatedVitalSigns == null) {
                logger.warn("PUT /api/vital-signs/{} - Vital signs not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, "Vital signs not found")
                );
            }
            logger.info("PUT /api/vital-signs/{} - Successfully updated vital signs", id);
            return ResponseEntity.ok(updatedVitalSigns);
        } catch (ServiceException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("not found") || message.contains("does not exist")) {
                logger.warn("PUT /api/vital-signs/{} - Update failed (not found): {}", id, e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage())
                );
            }
            logger.error("PUT /api/vital-signs/{} - Error updating vital signs: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        logger.debug("DELETE /api/vital-signs/{} - Deleting vital signs", id);
        try {
            vitalSignsService.delete(id);
            logger.info("DELETE /api/vital-signs/{} - Successfully deleted vital signs", id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.deleteSuccess(VITAL_SIGNS)
                    )
            );
        } catch (ServiceException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("not found") || message.contains("does not exist")) {
                logger.warn("DELETE /api/vital-signs/{} - Delete failed (not found): {}", id, e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage())
                );
            }
            logger.error("DELETE /api/vital-signs/{} - Error deleting vital signs: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }
}

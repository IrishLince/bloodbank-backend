package RedSource.controllers;

import RedSource.entities.MedicalAssessment;
import RedSource.entities.utils.MessageUtils;
import RedSource.entities.utils.ResponseUtils;
import RedSource.exceptions.ServiceException;
import RedSource.services.MedicalAssessmentService;
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
@RequestMapping("/api/medical-assessments")
@RequiredArgsConstructor
public class MedicalAssessmentController {

    private static final Logger logger = LoggerFactory.getLogger(MedicalAssessmentController.class);

    public static final String MEDICAL_ASSESSMENT = "Medical Assessment";
    public static final String MEDICAL_ASSESSMENTS = "Medical Assessments";

    private final MedicalAssessmentService medicalAssessmentService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        logger.debug("GET /api/medical-assessments - Retrieving all medical assessments");
        try {
            List<MedicalAssessment> assessments = medicalAssessmentService.getAll();
            logger.info("GET /api/medical-assessments - Successfully retrieved {} medical assessments", assessments.size());
            return ResponseEntity.ok(assessments);
        } catch (ServiceException e) {
            logger.error("GET /api/medical-assessments - Error retrieving medical assessments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        logger.debug("GET /api/medical-assessments/{} - Retrieving medical assessment by ID", id);
        try {
            MedicalAssessment assessment = medicalAssessmentService.getById(id);
            if (assessment == null) {
                logger.warn("GET /api/medical-assessments/{} - Medical assessment not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, "Medical assessment not found")
                );
            }
            logger.info("GET /api/medical-assessments/{} - Successfully retrieved medical assessment", id);
            return ResponseEntity.ok(assessment);
        } catch (ServiceException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("not found") || message.contains("does not exist")) {
                logger.warn("GET /api/medical-assessments/{} - Medical assessment not found: {}", id, e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage())
                );
            }
            logger.error("GET /api/medical-assessments/{} - Error retrieving medical assessment: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody MedicalAssessment assessment) {
        logger.debug("POST /api/medical-assessments - Creating new medical assessment");
        try {
            MedicalAssessment savedAssessment = medicalAssessmentService.save(assessment);
            logger.info("POST /api/medical-assessments - Successfully created medical assessment (ID: {})", savedAssessment.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAssessment);
        } catch (ServiceException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("not found") || message.contains("does not exist")) {
                logger.warn("POST /api/medical-assessments - Creation failed (not found): {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage())
                );
            }
            logger.error("POST /api/medical-assessments - Error creating medical assessment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody MedicalAssessment assessment) {
        logger.debug("PUT /api/medical-assessments/{} - Updating medical assessment", id);
        try {
            MedicalAssessment updatedAssessment = medicalAssessmentService.update(id, assessment);
            if (updatedAssessment == null) {
                logger.warn("PUT /api/medical-assessments/{} - Medical assessment not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, "Medical assessment not found")
                );
            }
            logger.info("PUT /api/medical-assessments/{} - Successfully updated medical assessment", id);
            return ResponseEntity.ok(updatedAssessment);
        } catch (ServiceException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("not found") || message.contains("does not exist")) {
                logger.warn("PUT /api/medical-assessments/{} - Update failed (not found): {}", id, e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage())
                );
            }
            logger.error("PUT /api/medical-assessments/{} - Error updating medical assessment: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        logger.debug("DELETE /api/medical-assessments/{} - Deleting medical assessment", id);
        try {
            medicalAssessmentService.delete(id);
            logger.info("DELETE /api/medical-assessments/{} - Successfully deleted medical assessment", id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.deleteSuccess(MEDICAL_ASSESSMENT)
                    )
            );
        } catch (ServiceException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("not found") || message.contains("does not exist")) {
                logger.warn("DELETE /api/medical-assessments/{} - Delete failed (not found): {}", id, e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage())
                );
            }
            logger.error("DELETE /api/medical-assessments/{} - Error deleting medical assessment: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }
}

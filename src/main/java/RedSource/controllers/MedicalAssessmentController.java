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

@RestController
@RequestMapping("/api/medical-assessments")
@RequiredArgsConstructor
public class MedicalAssessmentController {

    public static final String MEDICAL_ASSESSMENT = "Medical Assessment";
    public static final String MEDICAL_ASSESSMENTS = "Medical Assessments";

    private final MedicalAssessmentService medicalAssessmentService;

    @GetMapping
    public ResponseEntity<List<MedicalAssessment>> getAll() {
        try {
            List<MedicalAssessment> assessments = medicalAssessmentService.getAll();
            return ResponseEntity.ok(assessments);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalAssessment> getById(@PathVariable String id) {
        try {
            MedicalAssessment assessment = medicalAssessmentService.getById(id);
            if (assessment == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(assessment);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<MedicalAssessment> save(@RequestBody MedicalAssessment assessment) {
        try {
            MedicalAssessment savedAssessment = medicalAssessmentService.save(assessment);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAssessment);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicalAssessment> update(@PathVariable String id, @RequestBody MedicalAssessment assessment) {
        try {
            MedicalAssessment updatedAssessment = medicalAssessmentService.update(id, assessment);
            if (updatedAssessment == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updatedAssessment);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        try {
            medicalAssessmentService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

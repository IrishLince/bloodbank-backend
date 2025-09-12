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

@RestController
@RequestMapping("/api/vital-signs")
@RequiredArgsConstructor
public class VitalSignsController {

    public static final String VITAL_SIGNS = "Vital Signs";
    public static final String VITAL_SIGNS_PLURAL = "Vital Signs";

    private final VitalSignsService vitalSignsService;

    @GetMapping
    public ResponseEntity<List<VitalSigns>> getAll() {
        try {
            List<VitalSigns> vitalSigns = vitalSignsService.getAll();
            return ResponseEntity.ok(vitalSigns);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<VitalSigns> getById(@PathVariable String id) {
        try {
            VitalSigns vitalSigns = vitalSignsService.getById(id);
            if (vitalSigns == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(vitalSigns);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<VitalSigns> save(@RequestBody VitalSigns vitalSigns) {
        try {
            VitalSigns savedVitalSigns = vitalSignsService.save(vitalSigns);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedVitalSigns);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<VitalSigns> update(@PathVariable String id, @RequestBody VitalSigns vitalSigns) {
        try {
            VitalSigns updatedVitalSigns = vitalSignsService.update(id, vitalSigns);
            if (updatedVitalSigns == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updatedVitalSigns);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        try {
            vitalSignsService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

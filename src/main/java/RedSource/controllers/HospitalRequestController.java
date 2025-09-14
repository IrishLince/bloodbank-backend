package RedSource.controllers;

import RedSource.entities.HospitalRequest;
import RedSource.entities.utils.MessageUtils;
import RedSource.exceptions.ServiceException;
import RedSource.services.HospitalRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

@PreAuthorize("hasRole('HOSPITAL') or hasRole('ADMIN')")
@RestController
@RequestMapping("/api/hospital-requests")
@RequiredArgsConstructor
public class HospitalRequestController {

    private final HospitalRequestService hospitalRequestService;

    @GetMapping
    public ResponseEntity<List<HospitalRequest>> getAll() {
        try {
            List<HospitalRequest> requests = hospitalRequestService.getAll();
            return ResponseEntity.ok(requests);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<HospitalRequest> getById(@PathVariable String id) {
        try {
            HospitalRequest request = hospitalRequestService.getById(id);
            if (request == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(request);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<HospitalRequest> save(@RequestBody HospitalRequest request) {
        try {
            HospitalRequest savedRequest = hospitalRequestService.save(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRequest);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<HospitalRequest> update(@PathVariable String id, @RequestBody HospitalRequest request) {
        try {
            HospitalRequest updatedRequest = hospitalRequestService.update(id, request);
            if (updatedRequest == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updatedRequest);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            hospitalRequestService.delete(id);
            
            // Create a HashMap instead of Map.of() for more flexibility
            HashMap<String, Object> response = new HashMap<>();
            response.put("status", true);
            response.put("statusCode", 200);
            response.put("message", "Successfully deleted Hospital Request.");
            response.put("data", null);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Create a HashMap instead of Map.of() for more flexibility
            HashMap<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to process request: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
}

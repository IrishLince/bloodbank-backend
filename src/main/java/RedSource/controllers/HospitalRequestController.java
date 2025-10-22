package RedSource.controllers;

import RedSource.entities.HospitalRequest;
import RedSource.exceptions.ServiceException;
import RedSource.services.HospitalRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PreAuthorize("hasRole('HOSPITAL') or hasRole('ADMIN') or hasRole('BLOODBANK')")
@RestController
@RequestMapping("/api/hospital-requests")
@RequiredArgsConstructor
public class HospitalRequestController {

    private static final Logger log = LoggerFactory.getLogger(HospitalRequestController.class);

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

    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<List<HospitalRequest>> getByHospitalId(@PathVariable String hospitalId) {
        try {
            log.info("Received request for hospitalId: {}", hospitalId);
            List<HospitalRequest> requests = hospitalRequestService.findByHospitalId(hospitalId);
            log.info("Found {} requests for hospitalId: {}", requests.size(), hospitalId);
            return ResponseEntity.ok(requests);
        } catch (ServiceException e) {
            log.error("Error fetching requests for hospitalId: {}", hospitalId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/bloodbank/{bloodBankId}")
    public ResponseEntity<?> getByBloodBankId(@PathVariable String bloodBankId) {
        try {
            log.info("Received request for bloodBankId: {}", bloodBankId);
            List<HospitalRequest> requests = hospitalRequestService.findByBloodBankId(bloodBankId);
            log.info("Found {} requests for bloodBankId: {}", requests.size(), bloodBankId);
            
            // Create response in the expected format
            HashMap<String, Object> response = new HashMap<>();
            response.put("status", true);
            response.put("statusCode", 200);
            response.put("message", "Successfully retrieved Hospital Requests.");
            response.put("data", requests);
            
            return ResponseEntity.ok(response);
        } catch (ServiceException e) {
            log.error("Error fetching requests for bloodBankId: {}", bloodBankId, e);
            
            HashMap<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", false);
            errorResponse.put("message", "Failed to fetch requests: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestBody HashMap<String, String> statusUpdate) {
        try {
            log.info("Updating status for request: {}", id);
            String newStatus = statusUpdate.get("status");
            
            if (newStatus == null || newStatus.isEmpty()) {
                HashMap<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", false);
                errorResponse.put("message", "Status is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            HospitalRequest request = hospitalRequestService.getById(id);
            if (request == null) {
                return ResponseEntity.notFound().build();
            }
            
            request.setStatus(newStatus);
            HospitalRequest updatedRequest = hospitalRequestService.update(id, request);
            
            HashMap<String, Object> response = new HashMap<>();
            response.put("status", true);
            response.put("statusCode", 200);
            response.put("message", "Successfully updated request status.");
            response.put("data", updatedRequest);
            
            return ResponseEntity.ok(response);
        } catch (ServiceException e) {
            log.error("Error updating status for request: {}", id, e);
            
            HashMap<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", false);
            errorResponse.put("message", "Failed to update status: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
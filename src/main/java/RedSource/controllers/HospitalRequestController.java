package RedSource.controllers;

import RedSource.entities.HospitalRequest;
import RedSource.entities.utils.ResponseUtils;
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
    public ResponseEntity<?> getAll() {
        log.debug("GET /api/hospital-requests - Retrieving all hospital requests");
        try {
            List<HospitalRequest> requests = hospitalRequestService.getAll();
            log.info("GET /api/hospital-requests - Successfully retrieved {} hospital requests", requests.size());
            return ResponseEntity.ok(requests);
        } catch (ServiceException e) {
            log.error("GET /api/hospital-requests - Error retrieving hospital requests: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        log.debug("GET /api/hospital-requests/{} - Retrieving hospital request by ID", id);
        try {
            HospitalRequest request = hospitalRequestService.getById(id);
            if (request == null) {
                log.warn("GET /api/hospital-requests/{} - Hospital request not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, "Hospital request not found")
                );
            }
            log.info("GET /api/hospital-requests/{} - Successfully retrieved hospital request", id);
            return ResponseEntity.ok(request);
        } catch (ServiceException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("not found") || message.contains("does not exist")) {
                log.warn("GET /api/hospital-requests/{} - Hospital request not found: {}", id, e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage())
                );
            }
            log.error("GET /api/hospital-requests/{} - Error retrieving hospital request: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody HospitalRequest request) {
        log.debug("POST /api/hospital-requests - Creating new hospital request");
        try {
            HospitalRequest savedRequest = hospitalRequestService.save(request);
            log.info("POST /api/hospital-requests - Successfully created hospital request (ID: {})", savedRequest.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRequest);
        } catch (ServiceException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("not found") || message.contains("does not exist")) {
                log.warn("POST /api/hospital-requests - Creation failed (not found): {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage())
                );
            }
            log.error("POST /api/hospital-requests - Error creating hospital request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody HospitalRequest request) {
        log.debug("PUT /api/hospital-requests/{} - Updating hospital request", id);
        try {
            HospitalRequest updatedRequest = hospitalRequestService.update(id, request);
            if (updatedRequest == null) {
                log.warn("PUT /api/hospital-requests/{} - Hospital request not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, "Hospital request not found")
                );
            }
            log.info("PUT /api/hospital-requests/{} - Successfully updated hospital request", id);
            return ResponseEntity.ok(updatedRequest);
        } catch (ServiceException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("not found") || message.contains("does not exist")) {
                log.warn("PUT /api/hospital-requests/{} - Update failed (not found): {}", id, e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage())
                );
            }
            log.error("PUT /api/hospital-requests/{} - Error updating hospital request: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        log.debug("DELETE /api/hospital-requests/{} - Deleting hospital request", id);
        try {
            hospitalRequestService.delete(id);
            log.info("DELETE /api/hospital-requests/{} - Successfully deleted hospital request", id);

            // Create a HashMap instead of Map.of() for more flexibility
            HashMap<String, Object> response = new HashMap<>();
            response.put("status", true);
            response.put("statusCode", 200);
            response.put("message", "Successfully deleted Hospital Request.");
            response.put("data", null);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("DELETE /api/hospital-requests/{} - Error deleting hospital request: {}", id, e.getMessage(), e);
            // Create a HashMap instead of Map.of() for more flexibility
            HashMap<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to process request: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<?> getByHospitalId(@PathVariable String hospitalId) {
        log.debug("GET /api/hospital-requests/hospital/{} - Retrieving hospital requests by hospital ID", hospitalId);
        try {
            List<HospitalRequest> requests = hospitalRequestService.findByHospitalId(hospitalId);
            log.info("GET /api/hospital-requests/hospital/{} - Found {} requests for hospital", hospitalId, requests.size());
            return ResponseEntity.ok(requests);
        } catch (ServiceException e) {
            log.error("GET /api/hospital-requests/hospital/{} - Error fetching requests: {}", hospitalId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @GetMapping("/bloodbank/{bloodBankId}")
    public ResponseEntity<?> getByBloodBankId(@PathVariable String bloodBankId) {
        log.debug("GET /api/hospital-requests/bloodbank/{} - Retrieving hospital requests by blood bank ID", bloodBankId);
        try {
            List<HospitalRequest> requests = hospitalRequestService.findByBloodBankId(bloodBankId);
            log.info("GET /api/hospital-requests/bloodbank/{} - Found {} requests for blood bank", bloodBankId, requests.size());
            
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
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, "Hospital request not found")
                );
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
            String message = e.getMessage().toLowerCase();
            HttpStatus status = (message.contains("not found") || message.contains("does not exist")) 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(
                    ResponseUtils.buildErrorResponse(status, e.getMessage())
            );
        }
    }
}
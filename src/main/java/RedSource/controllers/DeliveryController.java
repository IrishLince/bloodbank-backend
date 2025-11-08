package RedSource.controllers;

import RedSource.entities.Delivery;
import RedSource.entities.HospitalRequest;
import RedSource.entities.utils.MessageUtils;
import RedSource.exceptions.ServiceException;
import RedSource.services.DeliveryService;
import RedSource.services.HospitalRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import RedSource.entities.utils.ResponseUtils;

@PreAuthorize("hasRole('ADMIN') or hasRole('BLOODBANK') or hasRole('HOSPITAL')")
@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Slf4j
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final HospitalRequestService hospitalRequestService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        log.debug("GET /api/deliveries - Retrieving all deliveries");
        try {
            List<Delivery> deliveries = deliveryService.getAll();
            log.info("GET /api/deliveries - Successfully retrieved {} deliveries", deliveries.size());
            return ResponseEntity.ok(deliveries);
        } catch (ServiceException e) {
            log.error("GET /api/deliveries - Error retrieving deliveries: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        log.debug("GET /api/deliveries/{} - Retrieving delivery by ID", id);
        try {
            Delivery delivery = deliveryService.getById(id);
            if (delivery == null) {
                log.warn("GET /api/deliveries/{} - Delivery not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, "Delivery not found")
                );
            }
            log.info("GET /api/deliveries/{} - Successfully retrieved delivery", id);
            return ResponseEntity.ok(delivery);
        } catch (ServiceException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("not found") || message.contains("does not exist")) {
                log.warn("GET /api/deliveries/{} - Delivery not found: {}", id, e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage())
                );
            }
            log.error("GET /api/deliveries/{} - Error retrieving delivery: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody Delivery delivery) {
        log.debug("POST /api/deliveries - Creating new delivery");
        try {
            Delivery savedDelivery = deliveryService.save(delivery);
            log.info("POST /api/deliveries - Successfully created delivery (ID: {})", savedDelivery.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDelivery);
        } catch (ServiceException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("not found") || message.contains("does not exist")) {
                log.warn("POST /api/deliveries - Delivery creation failed (not found): {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage())
                );
            }
            log.error("POST /api/deliveries - Error creating delivery: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Delivery delivery) {
        log.debug("PUT /api/deliveries/{} - Updating delivery", id);
        try {
            Delivery updatedDelivery = deliveryService.update(id, delivery);
            if (updatedDelivery == null) {
                log.warn("PUT /api/deliveries/{} - Delivery not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, "Delivery not found")
                );
            }
            log.info("PUT /api/deliveries/{} - Successfully updated delivery", id);
            return ResponseEntity.ok(updatedDelivery);
        } catch (ServiceException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("not found") || message.contains("does not exist")) {
                log.warn("PUT /api/deliveries/{} - Update failed (not found): {}", id, e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage())
                );
            }
            log.error("PUT /api/deliveries/{} - Error updating delivery: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        log.debug("DELETE /api/deliveries/{} - Deleting delivery", id);
        try {
            deliveryService.delete(id);
            log.info("DELETE /api/deliveries/{} - Successfully deleted delivery", id);
            HashMap<String, Object> response = new HashMap<>();
            response.put("status", true);
            response.put("statusCode", 200);
            response.put("message", MessageUtils.deleteSuccess("Delivery"));
            response.put("data", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("DELETE /api/deliveries/{} - Error deleting delivery: {}", id, e.getMessage(), e);
            HashMap<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to process request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<?> getByRequestId(@PathVariable String requestId) {
        try {
            List<Delivery> deliveries = deliveryService.findByRequestId(requestId);
            return ResponseEntity.ok(deliveries);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getAllByStatus(@PathVariable String status) {
        try {
            List<Delivery> deliveries = deliveryService.findAllByStatus(status);
            return ResponseEntity.ok(deliveries);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @PostMapping("/by-request-ids")
    public ResponseEntity<?> getDeliveriesByRequestIds(@RequestBody List<String> requestIds) {
        try {
            List<Delivery> deliveries = deliveryService.getDeliveriesByRequestIds(requestIds);
            return ResponseEntity.ok(deliveries);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateDeliveryStatus(@PathVariable String id,
            @RequestBody Map<String, String> statusUpdate) {
        log.debug("PUT /api/deliveries/{}/status - Updating delivery status", id);
        try {
            String newStatus = statusUpdate.get("status");
            if (newStatus == null || newStatus.trim().isEmpty()) {
                log.warn("PUT /api/deliveries/{}/status - Status is required", id);
                return ResponseEntity.badRequest().body(
                        ResponseUtils.buildErrorResponse(HttpStatus.BAD_REQUEST, "Status is required"));
            }

            Delivery delivery = deliveryService.getById(id);
            if (delivery == null) {
                log.warn("PUT /api/deliveries/{}/status - Delivery not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(HttpStatus.NOT_FOUND, "Delivery not found")
                );
            }

            // Update status and timestamp
            delivery.setStatus(newStatus);
            delivery.setUpdatedAt(new Date());

            // If marking as complete, also update delivered date/time and sync hospital
            // request
            if ("COMPLETE".equalsIgnoreCase(newStatus)) {
                delivery.setDeliveredDate(new Date());
                delivery.setDeliveredTime(new SimpleDateFormat("HH:mm:ss").format(new Date()));

                // Update the associated hospital request status to FULFILLED
                if (delivery.getRequestId() != null && !delivery.getRequestId().isEmpty()
                        && !"N/A".equals(delivery.getRequestId())) {
                    try {
                        HospitalRequest request = hospitalRequestService.getById(delivery.getRequestId());
                        if (request != null) {
                            request.setStatus("FULFILLED");
                            request.setUpdatedAt(new Date());
                            hospitalRequestService.update(delivery.getRequestId(), request);
                            log.info("Updated hospital request {} status to FULFILLED", delivery.getRequestId());
                        }
                    } catch (Exception e) {
                        log.warn("Could not update hospital request status: {}", e.getMessage());
                    }
                }
            }

            Delivery updatedDelivery = deliveryService.update(id, delivery);
            log.info("PUT /api/deliveries/{}/status - Successfully updated delivery status to {}", id, newStatus);

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Delivery status updated successfully",
                            updatedDelivery));
        } catch (ServiceException e) {
            String message = e.getMessage().toLowerCase();
            HttpStatus status = (message.contains("not found") || message.contains("does not exist")) 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            if (status == HttpStatus.NOT_FOUND) {
                log.warn("PUT /api/deliveries/{}/status - Update failed (not found): {}", id, e.getMessage());
            } else {
                log.error("PUT /api/deliveries/{}/status - Error updating delivery status: {}", id, e.getMessage(), e);
            }
            return ResponseEntity.status(status).body(
                    ResponseUtils.buildErrorResponse(status, e.getMessage())
            );
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchDeliveries(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String status) {
        try {
            List<Delivery> deliveries = deliveryService.searchDeliveries(searchTerm, status);
            return ResponseEntity.ok(deliveries);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }

    @PostMapping("/link-to-requests")
    public ResponseEntity<?> linkDeliveriesToRequests() {
        try {
            List<Delivery> deliveries = deliveryService.getAll();
            List<HospitalRequest> requests = hospitalRequestService.getAll();
            int linkedCount = 0;

            for (Delivery delivery : deliveries) {
                if (delivery.getRequestId() == null || delivery.getRequestId().equals("N/A")
                        || delivery.getRequestId().isEmpty()) {
                    // Find a matching hospital request
                    for (HospitalRequest request : requests) {
                        // Link based on hospital name or other criteria
                        if (request.getHospitalName() != null &&
                                delivery.getHospitalName() != null &&
                                request.getHospitalName().equals(delivery.getHospitalName())) {

                            delivery.setRequestId(request.getId());
                            delivery.setBloodBankName(request.getBloodBankName());
                            delivery.setBloodBankAddress(request.getBloodBankAddress());
                            delivery.setBloodBankPhone(request.getBloodBankPhone());
                            delivery.setBloodBankEmail(request.getBloodBankEmail());
                            delivery.setContactInfo(request.getContactInformation());

                            deliveryService.update(delivery.getId(), delivery);
                            linkedCount++;
                            break; // Link to first matching request
                        }
                    }
                }
            }

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Deliveries linked to requests successfully",
                            Map.of("linkedDeliveries", linkedCount)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to link deliveries to requests: " + e.getMessage()));
        }
    }

    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<?> getDeliveriesByHospital(@PathVariable String hospitalId) {
        try {
            List<Delivery> deliveries = deliveryService.getAll();

            // Filter deliveries for the specific hospital
            // This can be done by hospital name matching or by linked requests
            List<Delivery> hospitalDeliveries = deliveries.stream()
                    .filter(delivery -> {
                        // For now, we'll populate some sample hospital data
                        // In a real system, this would be linked properly through hospital requests
                        return delivery.getHospitalName() != null;
                    })
                    .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(hospitalDeliveries);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())
            );
        }
    }
}

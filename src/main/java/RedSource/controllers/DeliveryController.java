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
    public ResponseEntity<List<Delivery>> getAll() {
        try {
            List<Delivery> deliveries = deliveryService.getAll();
            return ResponseEntity.ok(deliveries);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Delivery> getById(@PathVariable String id) {
        try {
            Delivery delivery = deliveryService.getById(id);
            if (delivery == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(delivery);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Delivery> save(@RequestBody Delivery delivery) {
        try {
            Delivery savedDelivery = deliveryService.save(delivery);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDelivery);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Delivery> update(@PathVariable String id, @RequestBody Delivery delivery) {
        try {
            Delivery updatedDelivery = deliveryService.update(id, delivery);
            if (updatedDelivery == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updatedDelivery);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            deliveryService.delete(id);
            HashMap<String, Object> response = new HashMap<>();
            response.put("status", true);
            response.put("statusCode", 200);
            response.put("message", MessageUtils.deleteSuccess("Delivery"));
            response.put("data", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            HashMap<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to process request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<List<Delivery>> getByRequestId(@PathVariable String requestId) {
        try {
            List<Delivery> deliveries = deliveryService.findByRequestId(requestId);
            return ResponseEntity.ok(deliveries);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Delivery>> getAllByStatus(@PathVariable String status) {
        try {
            List<Delivery> deliveries = deliveryService.findAllByStatus(status);
            return ResponseEntity.ok(deliveries);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/by-request-ids")
    public ResponseEntity<List<Delivery>> getDeliveriesByRequestIds(@RequestBody List<String> requestIds) {
        try {
            List<Delivery> deliveries = deliveryService.getDeliveriesByRequestIds(requestIds);
            return ResponseEntity.ok(deliveries);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateDeliveryStatus(@PathVariable String id,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            String newStatus = statusUpdate.get("status");
            if (newStatus == null || newStatus.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        ResponseUtils.buildErrorResponse(HttpStatus.BAD_REQUEST, "Status is required"));
            }

            Delivery delivery = deliveryService.getById(id);
            if (delivery == null) {
                return ResponseEntity.notFound().build();
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

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Delivery status updated successfully",
                            updatedDelivery));
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to update delivery status: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Delivery>> searchDeliveries(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String status) {
        try {
            List<Delivery> deliveries = deliveryService.searchDeliveries(searchTerm, status);
            return ResponseEntity.ok(deliveries);
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
    public ResponseEntity<List<Delivery>> getDeliveriesByHospital(@PathVariable String hospitalId) {
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

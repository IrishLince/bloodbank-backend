package RedSource.controllers;

import RedSource.entities.Delivery;
import RedSource.entities.utils.MessageUtils;
import RedSource.exceptions.ServiceException;
import RedSource.services.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@PreAuthorize("hasRole('ADMIN') or hasRole('BLOODBANK') or hasRole('HOSPITAL')")
@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

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
}

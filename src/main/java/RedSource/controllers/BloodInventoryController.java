package RedSource.controllers;

import RedSource.entities.BloodInventory;
import RedSource.entities.utils.MessageUtils;
import RedSource.entities.utils.ResponseUtils;
import RedSource.services.BloodInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/blood-inventory")
@RequiredArgsConstructor
public class BloodInventoryController {

    private static final Logger logger = LoggerFactory.getLogger(BloodInventoryController.class);

    public static final String BLOOD_INVENTORY = "Blood Inventory";
    public static final String BLOOD_INVENTORIES = "Blood Inventories";

    private final BloodInventoryService bloodInventoryService;

    // Get all blood inventories
    @GetMapping
    public ResponseEntity<?> getAll() {
        logger.debug("GET /api/blood-inventory - Retrieving all blood inventories");
        try {
            var inventories = bloodInventoryService.getAll();
            logger.info("GET /api/blood-inventory - Successfully retrieved {} blood inventories", inventories.size());
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(BLOOD_INVENTORIES),
                            inventories
                    )
            );
        } catch (Exception e) {
            logger.error("GET /api/blood-inventory - Error retrieving blood inventories: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Get blood inventory by blood bank ID
    @GetMapping("/bloodbank/{bloodBankId}")
    public ResponseEntity<?> getByBloodBankId(@PathVariable String bloodBankId) {
        logger.debug("GET /api/blood-inventory/bloodbank/{} - Retrieving blood inventories by blood bank ID", bloodBankId);
        try {
            var inventories = bloodInventoryService.getByBloodBankId(bloodBankId);
            logger.info("GET /api/blood-inventory/bloodbank/{} - Successfully retrieved {} blood inventories", bloodBankId, inventories.size());
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(BLOOD_INVENTORIES),
                            inventories
                    )
            );
        } catch (Exception e) {
            logger.error("GET /api/blood-inventory/bloodbank/{} - Error retrieving blood inventories: {}", bloodBankId, e.getMessage(), e);
            throw e;
        }
    }

    // Get blood inventory by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        logger.debug("GET /api/blood-inventory/{} - Retrieving blood inventory by ID", id);
        try {
            BloodInventory bloodInventory = bloodInventoryService.getById(id);
            if (bloodInventory == null) {
                logger.warn("GET /api/blood-inventory/{} - Blood inventory not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Blood inventory not found"
                        )
                );
            }
            logger.info("GET /api/blood-inventory/{} - Successfully retrieved blood inventory", id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(BLOOD_INVENTORY),
                            bloodInventory
                    )
            );
        } catch (Exception e) {
            logger.error("GET /api/blood-inventory/{} - Error retrieving blood inventory: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // Save a new blood inventory
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody BloodInventory bloodInventory, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            MessageUtils.validationErrors(bindingResult)
                    )
            );
        }
        logger.debug("POST /api/blood-inventory - Creating new blood inventory");
        try {
            BloodInventory savedBloodInventory = bloodInventoryService.save(bloodInventory);
            logger.info("POST /api/blood-inventory - Successfully created blood inventory (ID: {})", savedBloodInventory.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.CREATED,
                            MessageUtils.saveSuccess(BLOOD_INVENTORY),
                            savedBloodInventory
                    )
            );
        } catch (Exception e) {
            logger.error("POST /api/blood-inventory - Error creating blood inventory: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Update an existing blood inventory
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody BloodInventory bloodInventory, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            MessageUtils.validationErrors(bindingResult)
                    )
            );
        }
        logger.debug("PUT /api/blood-inventory/{} - Updating blood inventory", id);
        try {
            BloodInventory updatedBloodInventory = bloodInventoryService.update(id, bloodInventory);
            if (updatedBloodInventory == null) {
                logger.warn("PUT /api/blood-inventory/{} - Blood inventory not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Blood inventory not found"
                        )
                );
            }
            logger.info("PUT /api/blood-inventory/{} - Successfully updated blood inventory", id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.updateSuccess(BLOOD_INVENTORY),
                            updatedBloodInventory
                    )
            );
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            HttpStatus status = (message.contains("not found") || message.contains("does not exist")) 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            if (status == HttpStatus.NOT_FOUND) {
                logger.warn("PUT /api/blood-inventory/{} - Update failed (not found): {}", id, e.getMessage());
            } else {
                logger.error("PUT /api/blood-inventory/{} - Error updating blood inventory: {}", id, e.getMessage(), e);
            }
            return ResponseEntity.status(status).body(
                    ResponseUtils.buildErrorResponse(status, e.getMessage())
            );
        }
    }

    // Delete a blood inventory
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        logger.debug("DELETE /api/blood-inventory/{} - Deleting blood inventory", id);
        try {
            bloodInventoryService.delete(id);
            logger.info("DELETE /api/blood-inventory/{} - Successfully deleted blood inventory", id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.deleteSuccess(BLOOD_INVENTORY)
                    )
            );
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            HttpStatus status = (message.contains("not found") || message.contains("does not exist")) 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            if (status == HttpStatus.NOT_FOUND) {
                logger.warn("DELETE /api/blood-inventory/{} - Delete failed (not found): {}", id, e.getMessage());
            } else {
                logger.error("DELETE /api/blood-inventory/{} - Error deleting blood inventory: {}", id, e.getMessage(), e);
            }
            return ResponseEntity.status(status).body(
                    ResponseUtils.buildErrorResponse(status, e.getMessage())
            );
        }
    }
}

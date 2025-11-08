package RedSource.services;

import RedSource.entities.BloodInventory;
import RedSource.entities.utils.MessageUtils;
import RedSource.exceptions.ServiceException;
import RedSource.repositories.BloodInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BloodInventoryService {

    private static final Logger log = LoggerFactory.getLogger(BloodInventoryService.class);
    private final BloodInventoryRepository bloodInventoryRepository;

    public List<BloodInventory> getAll() {
        try {
            List<BloodInventory> bloodInventories = bloodInventoryRepository.findAll();
            log.info(MessageUtils.retrieveSuccess("Blood Inventories"));
            return bloodInventories;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError("Blood Inventories");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public List<BloodInventory> getByBloodBankId(String bloodBankId) {
        try {
            List<BloodInventory> bloodInventories = bloodInventoryRepository.findByBloodBankId(bloodBankId);
            log.info(MessageUtils.retrieveSuccess("Blood Inventories for Blood Bank: " + bloodBankId));
            return bloodInventories;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError("Blood Inventories");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public BloodInventory getById(String id) {
        try {
            if (Objects.isNull(id)) {
                return null;
            }
            return bloodInventoryRepository.findById(id).orElse(null);
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError("Blood Inventory");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public BloodInventory save(BloodInventory bloodInventory) {
        try {
            bloodInventory.setCreatedAt(new Date());
            bloodInventory.setUpdatedAt(new Date());
            BloodInventory savedBloodInventory = bloodInventoryRepository.save(bloodInventory);
            log.info(MessageUtils.saveSuccess("Blood Inventory"));
            return savedBloodInventory;
        } catch (Exception e) {
            String errorMessage = MessageUtils.saveError("Blood Inventory");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public BloodInventory update(String id, BloodInventory bloodInventory) {
        try {
            BloodInventory existingBloodInventory = getById(id);
            if (existingBloodInventory == null) {
                throw new ServiceException("Blood Inventory not found");
            }
            
            // Auto-update status based on quantity changes
            int oldQuantity = existingBloodInventory.getQuantity() != null ? existingBloodInventory.getQuantity() : 0;
            int newQuantity = bloodInventory.getQuantity() != null ? bloodInventory.getQuantity() : 0;
            
            if (oldQuantity == 0 && newQuantity > 0) {
                bloodInventory.setStatus("Available");
                log.info("Inventory status automatically updated to Available (quantity changed from 0 to " + newQuantity + ")");
            } else if (oldQuantity > 0 && newQuantity == 0) {
                bloodInventory.setStatus("Unavailable");
                log.info("Inventory status automatically updated to Unavailable (quantity changed from " + oldQuantity + " to 0)");
            }
            
            bloodInventory.setId(id);
            bloodInventory.setCreatedAt(existingBloodInventory.getCreatedAt());
            bloodInventory.setUpdatedAt(new Date());
            BloodInventory updatedBloodInventory = bloodInventoryRepository.save(bloodInventory);
            log.info(MessageUtils.updateSuccess("Blood Inventory"));
            return updatedBloodInventory;
        } catch (Exception e) {
            String errorMessage = MessageUtils.updateError("Blood Inventory");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    /**
     * Get available units for a specific blood type at a blood bank
     */
    public int getAvailableUnits(String bloodBankId, String bloodTypeId) {
        try {
            List<BloodInventory> inventories = bloodInventoryRepository.findByBloodBankId(bloodBankId);
            
            // Sum up all available units for this blood type (only count "Available" status)
            int totalAvailableUnits = inventories.stream()
                    .filter(inv -> bloodTypeId.equals(inv.getBloodTypeId()))
                    .filter(inv -> "Available".equals(inv.getStatus())) // Only count available inventory
                    .mapToInt(inv -> inv.getQuantity() != null ? inv.getQuantity() : 0)
                    .sum();
            
            return totalAvailableUnits;
        } catch (Exception e) {
            log.error("Error getting available units: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Decrease blood inventory units for a specific blood type and blood bank
     * Note: bloodTypeId should match the blood type string (e.g., "A+", "O-")
     */
    public void decreaseInventory(String bloodBankId, String bloodTypeId, int units) {
        try {
            List<BloodInventory> inventories = bloodInventoryRepository.findByBloodBankId(bloodBankId);
            
            // Find all available inventory items for this blood type, sorted by quantity (descending)
            List<BloodInventory> availableInventories = inventories.stream()
                    .filter(inv -> bloodTypeId.equals(inv.getBloodTypeId()))
                    .filter(inv -> "Available".equals(inv.getStatus()))
                    .filter(inv -> inv.getQuantity() != null && inv.getQuantity() > 0)
                    .sorted((a, b) -> Integer.compare(b.getQuantity(), a.getQuantity())) // Sort by quantity descending
                    .toList();
            
            if (availableInventories.isEmpty()) {
                log.warn("No available inventory found for blood type " + bloodTypeId + " at blood bank " + bloodBankId);
                return;
            }
            
            // Calculate total available units
            int totalAvailable = availableInventories.stream()
                    .mapToInt(inv -> inv.getQuantity() != null ? inv.getQuantity() : 0)
                    .sum();
            
            if (totalAvailable < units) {
                log.warn("Insufficient units for blood type " + bloodTypeId + ". Available: " + totalAvailable + ", Requested: " + units);
                // Still allow the operation but log the warning
            }
            
            // Decrease units from available inventories (starting with the largest quantities)
            int remainingUnits = units;
            for (BloodInventory inventory : availableInventories) {
                if (remainingUnits <= 0) break;
                
                int currentQuantity = inventory.getQuantity() != null ? inventory.getQuantity() : 0;
                int unitsToDecrease = Math.min(remainingUnits, currentQuantity);
                
                int newQuantity = currentQuantity - unitsToDecrease;
                inventory.setQuantity(newQuantity);
                
                // Mark as Unavailable if quantity reaches 0
                if (newQuantity == 0) {
                    inventory.setStatus("Unavailable");
                    log.info("Inventory for " + bloodTypeId + " marked as Unavailable (0 units)");
                }
                
                inventory.setUpdatedAt(new Date());
                bloodInventoryRepository.save(inventory);
                
                remainingUnits -= unitsToDecrease;
                log.info("Decreased inventory for " + bloodTypeId + " by " + unitsToDecrease + " units (remaining: " + remainingUnits + ")");
            }
            
        } catch (Exception e) {
            log.error("Error decreasing blood inventory: " + e.getMessage(), e);
            // Don't throw exception - we don't want to fail the voucher completion if inventory update fails
        }
    }

    public void delete(String id) {
        try {
            BloodInventory bloodInventory = getById(id);
            if (bloodInventory == null) {
                throw new ServiceException("Blood Inventory not found");
            }
            bloodInventoryRepository.deleteById(id);
            log.info(MessageUtils.deleteSuccess("Blood Inventory"));
        } catch (Exception e) {
            String errorMessage = MessageUtils.deleteError("Blood Inventory");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }
} 
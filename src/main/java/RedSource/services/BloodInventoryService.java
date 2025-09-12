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
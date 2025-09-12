package RedSource.services;

import RedSource.entities.BloodBank;
import RedSource.entities.utils.MessageUtils;
import RedSource.exceptions.ServiceException;
import RedSource.repositories.BloodBankRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BloodBankService {

    private static final Logger log = LoggerFactory.getLogger(BloodBankService.class);
    private final BloodBankRepository bloodBankRepository;

    public List<BloodBank> getAll() {
        try {
            List<BloodBank> bloodBanks = bloodBankRepository.findAll();
            log.info(MessageUtils.retrieveSuccess("Blood Banks"));
            return bloodBanks;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError("Blood Banks");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public BloodBank getById(String id) {
        try {
            if (Objects.isNull(id)) {
                return null;
            }
            return bloodBankRepository.findById(id).orElse(null);
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError("Blood Bank");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public BloodBank save(BloodBank bloodBank) {
        try {
            bloodBank.setCreatedAt(new Date());
            bloodBank.setUpdatedAt(new Date());
            BloodBank savedBloodBank = bloodBankRepository.save(bloodBank);
            log.info(MessageUtils.saveSuccess("Blood Bank"));
            return savedBloodBank;
        } catch (Exception e) {
            String errorMessage = MessageUtils.saveError("Blood Bank");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public BloodBank update(String id, BloodBank bloodBank) {
        try {
            BloodBank existingBloodBank = getById(id);
            if (existingBloodBank == null) {
                throw new ServiceException("Blood Bank not found");
            }
            bloodBank.setId(id);
            bloodBank.setCreatedAt(existingBloodBank.getCreatedAt());
            bloodBank.setUpdatedAt(new Date());
            BloodBank updatedBloodBank = bloodBankRepository.save(bloodBank);
            log.info(MessageUtils.updateSuccess("Blood Bank"));
            return updatedBloodBank;
        } catch (Exception e) {
            String errorMessage = MessageUtils.updateError("Blood Bank");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public void delete(String id) {
        try {
            BloodBank bloodBank = getById(id);
            if (bloodBank == null) {
                throw new ServiceException("Blood Bank not found");
            }
            bloodBankRepository.deleteById(id);
            log.info(MessageUtils.deleteSuccess("Blood Bank"));
        } catch (Exception e) {
            String errorMessage = MessageUtils.deleteError("Blood Bank");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }
} 
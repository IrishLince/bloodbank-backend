package RedSource.services;

import RedSource.entities.BloodBankUser;
import RedSource.entities.utils.MessageUtils;
import RedSource.exceptions.ServiceException;
import RedSource.repositories.BloodBankUserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BloodBankUserService {

    private static final Logger log = LoggerFactory.getLogger(BloodBankUserService.class);
    private final BloodBankUserRepository bloodBankUserRepository;

    public List<BloodBankUser> getAll() {
        try {
            List<BloodBankUser> bloodBankUsers = bloodBankUserRepository.findAll();
            log.info(MessageUtils.retrieveSuccess("Blood Bank Users"));
            return bloodBankUsers;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError("Blood Bank Users");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public BloodBankUser getById(String id) {
        try {
            BloodBankUser bloodBankUser = bloodBankUserRepository.findById(id).orElse(null);
            if (Objects.nonNull(bloodBankUser)) {
                log.info(MessageUtils.retrieveSuccess("Blood Bank User"));
            }
            return bloodBankUser;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError("Blood Bank User");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public BloodBankUser getByEmail(String email) {
        try {
            BloodBankUser bloodBankUser = bloodBankUserRepository.findByEmail(email).orElse(null);
            if (Objects.nonNull(bloodBankUser)) {
                log.info(MessageUtils.retrieveSuccess("Blood Bank User"));
            }
            return bloodBankUser;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError("Blood Bank User");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public BloodBankUser save(BloodBankUser bloodBankUser) {
        try {
            if (Objects.isNull(bloodBankUser.getCreatedAt())) {
                bloodBankUser.setCreatedAt(new Date());
            }
            bloodBankUser.setUpdatedAt(new Date());
            
            BloodBankUser savedBloodBankUser = bloodBankUserRepository.save(bloodBankUser);
            log.info(MessageUtils.saveSuccess("Blood Bank User"));
            return savedBloodBankUser;
        } catch (Exception e) {
            String errorMessage = MessageUtils.saveError("Blood Bank User");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public BloodBankUser update(String id, BloodBankUser bloodBankUser) {
        try {
            BloodBankUser existingBloodBankUser = getById(id);
            if (Objects.isNull(existingBloodBankUser)) {
                throw new ServiceException("Blood Bank User not found");
            }

            // Update fields
            if (Objects.nonNull(bloodBankUser.getBloodBankName())) {
                existingBloodBankUser.setBloodBankName(bloodBankUser.getBloodBankName());
            }
            if (Objects.nonNull(bloodBankUser.getEmail())) {
                existingBloodBankUser.setEmail(bloodBankUser.getEmail());
            }
            if (Objects.nonNull(bloodBankUser.getPhone())) {
                existingBloodBankUser.setPhone(bloodBankUser.getPhone());
            }
            if (Objects.nonNull(bloodBankUser.getAddress())) {
                existingBloodBankUser.setAddress(bloodBankUser.getAddress());
            }
            if (Objects.nonNull(bloodBankUser.getOperatingHours())) {
                existingBloodBankUser.setOperatingHours(bloodBankUser.getOperatingHours());
            }
            if (Objects.nonNull(bloodBankUser.getProfilePhotoUrl())) {
                existingBloodBankUser.setProfilePhotoUrl(bloodBankUser.getProfilePhotoUrl());
            }

            existingBloodBankUser.setUpdatedAt(new Date());
            
            BloodBankUser updatedBloodBankUser = bloodBankUserRepository.save(existingBloodBankUser);
            log.info(MessageUtils.updateSuccess("Blood Bank User"));
            return updatedBloodBankUser;
        } catch (Exception e) {
            String errorMessage = MessageUtils.updateError("Blood Bank User");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public BloodBankUser updatePreferredBloodTypes(String id, List<String> preferredBloodTypes) {
        try {
            BloodBankUser existingBloodBankUser = getById(id);
            if (Objects.isNull(existingBloodBankUser)) {
                throw new ServiceException("Blood Bank User not found");
            }

            existingBloodBankUser.setPreferredBloodTypes(preferredBloodTypes);
            existingBloodBankUser.setUpdatedAt(new Date());
            
            BloodBankUser updatedBloodBankUser = bloodBankUserRepository.save(existingBloodBankUser);
            log.info("Preferred blood types updated for Blood Bank User: {}", id);
            return updatedBloodBankUser;
        } catch (Exception e) {
            String errorMessage = "Error updating preferred blood types";
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public void deleteById(String id) {
        try {
            BloodBankUser bloodBankUser = getById(id);
            if (Objects.isNull(bloodBankUser)) {
                throw new ServiceException("Blood Bank User not found");
            }
            
            bloodBankUserRepository.deleteById(id);
            log.info(MessageUtils.deleteSuccess("Blood Bank User"));
        } catch (Exception e) {
            String errorMessage = MessageUtils.deleteError("Blood Bank User");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }
}

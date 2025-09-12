package RedSource.services;

import RedSource.entities.VitalSigns;
import RedSource.entities.utils.MessageUtils;
import RedSource.exceptions.ServiceException;
import RedSource.repositories.VitalSignsRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VitalSignsService {

    private static final Logger log = LoggerFactory.getLogger(VitalSignsService.class);
    public static final String VITAL_SIGNS = "Vital Signs";
    public static final String VITAL_SIGN = "Vital Sign";

    private final VitalSignsRepository vitalSignsRepository;

    public List<VitalSigns> getAll() {
        try {
            List<VitalSigns> vitalSigns = vitalSignsRepository.findAll();
            log.info(MessageUtils.retrieveSuccess(VITAL_SIGNS));
            return vitalSigns;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(VITAL_SIGNS);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public VitalSigns getById(String id) {
        try {
            if (Objects.isNull(id)) {
                return null;
            }
            return vitalSignsRepository.findById(id).orElse(null);
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(VITAL_SIGN);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public VitalSigns save(VitalSigns vitalSigns) {
        try {
            Date now = new Date();
            vitalSigns.setCreatedAt(now);
            vitalSigns.setUpdatedAt(now);
            vitalSigns.setMeasurementDate(now);
            VitalSigns savedVitalSigns = vitalSignsRepository.save(vitalSigns);
            log.info(MessageUtils.saveSuccess(VITAL_SIGN));
            return savedVitalSigns;
        } catch (Exception e) {
            String errorMessage = MessageUtils.saveError(VITAL_SIGN);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public VitalSigns update(String id, VitalSigns vitalSigns) {
        try {
            VitalSigns existingVitalSigns = getById(id);
            if (existingVitalSigns == null) {
                throw new ServiceException("Vital Signs not found");
            }
            vitalSigns.setId(id);
            vitalSigns.setCreatedAt(existingVitalSigns.getCreatedAt());
            vitalSigns.setUpdatedAt(new Date());
            VitalSigns updatedVitalSigns = vitalSignsRepository.save(vitalSigns);
            log.info(MessageUtils.updateSuccess(VITAL_SIGN));
            return updatedVitalSigns;
        } catch (Exception e) {
            String errorMessage = MessageUtils.updateError(VITAL_SIGN);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public void delete(String id) {
        try {
            VitalSigns vitalSigns = getById(id);
            if (vitalSigns == null) {
                throw new ServiceException("Vital Signs not found");
            }
            vitalSignsRepository.deleteById(id);
            log.info(MessageUtils.deleteSuccess(VITAL_SIGN));
        } catch (Exception e) {
            String errorMessage = MessageUtils.deleteError(VITAL_SIGN);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }
} 
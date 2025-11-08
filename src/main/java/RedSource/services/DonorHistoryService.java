package RedSource.services;

import RedSource.entities.DonorHistory;
import RedSource.entities.utils.MessageUtils;
import RedSource.exceptions.ServiceException;
import RedSource.repositories.DonorHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DonorHistoryService {

    private static final Logger log = LoggerFactory.getLogger(DonorHistoryService.class);
    public static final String DONOR_HISTORIES = "Donor Histories";
    public static final String DONOR_HISTORY = "Donor History";

    private final DonorHistoryRepository donorHistoryRepository;

    public List<DonorHistory> getAll() {
        try {
            List<DonorHistory> donorHistories = donorHistoryRepository.findAll();
            log.info(MessageUtils.retrieveSuccess(DONOR_HISTORIES));
            return donorHistories;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(DONOR_HISTORIES);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public DonorHistory getById(String id) {
        try {
            if (Objects.isNull(id)) {
                return null;
            }
            return donorHistoryRepository.findById(id).orElse(null);
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(DONOR_HISTORY);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public List<DonorHistory> getByDonorId(String donorId) {
        try {
            if (Objects.isNull(donorId)) {
                return List.of();
            }
            List<DonorHistory> histories = donorHistoryRepository.findByDonorId(donorId);
            log.info(MessageUtils.retrieveSuccess(DONOR_HISTORIES));
            return histories;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(DONOR_HISTORIES);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public List<DonorHistory> getByBloodBankId(String bloodBankId) {
        try {
            if (Objects.isNull(bloodBankId)) {
                return List.of();
            }
            List<DonorHistory> histories = donorHistoryRepository.findByBloodBankId(bloodBankId);
            log.info(MessageUtils.retrieveSuccess(DONOR_HISTORIES));
            return histories;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(DONOR_HISTORIES);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public DonorHistory save(DonorHistory donorHistory) {
        try {
            donorHistory.setCreatedAt(new Date());
            donorHistory.setUpdatedAt(new Date());
            DonorHistory savedDonorHistory = donorHistoryRepository.save(donorHistory);
            log.info(MessageUtils.saveSuccess(DONOR_HISTORY));
            return savedDonorHistory;
        } catch (Exception e) {
            String errorMessage = MessageUtils.saveError(DONOR_HISTORY);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public DonorHistory update(String id, DonorHistory donorHistory) {
        try {
            DonorHistory existingDonorHistory = getById(id);
            if (existingDonorHistory == null) {
                throw new ServiceException("Donor History not found");
            }
            donorHistory.setId(id);
            donorHistory.setCreatedAt(existingDonorHistory.getCreatedAt());
            donorHistory.setUpdatedAt(new Date());
            DonorHistory updatedDonorHistory = donorHistoryRepository.save(donorHistory);
            log.info(MessageUtils.updateSuccess(DONOR_HISTORY));
            return updatedDonorHistory;
        } catch (Exception e) {
            String errorMessage = MessageUtils.updateError(DONOR_HISTORY);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public void delete(String id) {
        try {
            DonorHistory donorHistory = getById(id);
            if (donorHistory == null) {
                throw new ServiceException("Donor History not found");
            }
            donorHistoryRepository.deleteById(id);
            log.info(MessageUtils.deleteSuccess(DONOR_HISTORY));
        } catch (Exception e) {
            String errorMessage = MessageUtils.deleteError(DONOR_HISTORY);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }
} 
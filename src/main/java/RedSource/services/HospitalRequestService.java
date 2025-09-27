package RedSource.services;

import RedSource.entities.BloodBank; // Added this import
import RedSource.entities.HospitalRequest;
import RedSource.entities.utils.MessageUtils;
import RedSource.exceptions.ServiceException;
import RedSource.repositories.HospitalRequestRepository;
import RedSource.services.BloodBankService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class HospitalRequestService {

    private static final Logger log = LoggerFactory.getLogger(HospitalRequestService.class);
    public static final String HOSPITAL_REQUESTS = "Hospital Requests";
    public static final String HOSPITAL_REQUEST = "Hospital Request";

    private final HospitalRequestRepository hospitalRequestRepository;
    private final BloodBankService bloodBankService;

    public List<HospitalRequest> getAll() {
        try {
            List<HospitalRequest> requests = hospitalRequestRepository.findAll();
            log.info(MessageUtils.retrieveSuccess(HOSPITAL_REQUESTS));
            return requests;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(HOSPITAL_REQUESTS);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public HospitalRequest getById(String id) {
        try {
            if (Objects.isNull(id)) {
                return null;
            }
            return hospitalRequestRepository.findById(id).orElse(null);
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(HOSPITAL_REQUEST);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public HospitalRequest save(HospitalRequest request) {
        try {
            Date now = new Date();
            request.setCreatedAt(now);
            request.setUpdatedAt(now);
            HospitalRequest savedRequest = hospitalRequestRepository.save(request);
            log.info(MessageUtils.saveSuccess(HOSPITAL_REQUEST));
            return savedRequest;
        } catch (Exception e) {
            String errorMessage = MessageUtils.saveError(HOSPITAL_REQUEST);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public HospitalRequest update(String id, HospitalRequest request) {
        try {
            HospitalRequest existingRequest = getById(id);
            if (existingRequest == null) {
                throw new ServiceException("Hospital Request not found");
            }
            request.setId(id);
            request.setCreatedAt(existingRequest.getCreatedAt());
            request.setUpdatedAt(new Date());
            HospitalRequest updatedRequest = hospitalRequestRepository.save(request);
            log.info(MessageUtils.updateSuccess(HOSPITAL_REQUEST));
            return updatedRequest;
        } catch (Exception e) {
            String errorMessage = MessageUtils.updateError(HOSPITAL_REQUEST);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public void delete(String id) {
        try {
            HospitalRequest request = getById(id);
            if (request == null) {
                throw new ServiceException("Hospital Request not found");
            }
            hospitalRequestRepository.deleteById(id);
            log.info(MessageUtils.deleteSuccess(HOSPITAL_REQUEST));
        } catch (Exception e) {
            String errorMessage = MessageUtils.deleteError(HOSPITAL_REQUEST);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public List<HospitalRequest> findByHospitalId(String hospitalId) {
        try {
            List<HospitalRequest> requests = hospitalRequestRepository.findByHospitalId(hospitalId);
            
            // Enrich requests with blood bank details
            for (HospitalRequest request : requests) {
                if (request.getBloodBankId() != null) {
                    BloodBank bloodBank = bloodBankService.getById(request.getBloodBankId());
                    if (bloodBank != null) {
                        request.setBloodBankName(bloodBank.getName());
                        request.setBloodBankAddress(bloodBank.getAddress());
                        request.setContactInformation(bloodBank.getContactInformation());
                        request.setBloodBankPhone(bloodBank.getPhone());
                        request.setBloodBankEmail(bloodBank.getEmail());
                    }
                }
            }

            log.info(MessageUtils.retrieveSuccess(HOSPITAL_REQUESTS));
            return requests;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(HOSPITAL_REQUESTS);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }
}
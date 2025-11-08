package RedSource.services;

import RedSource.entities.Fulfillment;
import RedSource.entities.utils.MessageUtils;
import RedSource.exceptions.ServiceException;
import RedSource.repositories.FulfillmentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FulfillmentService {

    private static final Logger log = LoggerFactory.getLogger(FulfillmentService.class);
    private final FulfillmentRepository fulfillmentRepository;

    public List<Fulfillment> getAll() {
        try {
            List<Fulfillment> fulfillments = fulfillmentRepository.findAll();
            log.info(MessageUtils.retrieveSuccess("Fulfillments"));
            return fulfillments;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError("Fulfillments");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public Fulfillment getById(String id) {
        try {
            if (Objects.isNull(id)) {
                return null;
            }
            return fulfillmentRepository.findById(id).orElse(null);
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError("Fulfillment");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public Fulfillment save(Fulfillment fulfillment) {
        try {
            fulfillment.setCreatedAt(new Date());
            fulfillment.setUpdatedAt(new Date());
            Fulfillment savedFulfillment = fulfillmentRepository.save(fulfillment);
            log.info(MessageUtils.saveSuccess("Fulfillment"));
            return savedFulfillment;
        } catch (Exception e) {
            String errorMessage = MessageUtils.saveError("Fulfillment");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public Fulfillment update(String id, Fulfillment fulfillment) {
        try {
            Fulfillment existingFulfillment = getById(id);
            if (existingFulfillment == null) {
                throw new ServiceException("Fulfillment not found");
            }
            fulfillment.setId(id);
            fulfillment.setCreatedAt(existingFulfillment.getCreatedAt());
            fulfillment.setUpdatedAt(new Date());
            Fulfillment updatedFulfillment = fulfillmentRepository.save(fulfillment);
            log.info(MessageUtils.updateSuccess("Fulfillment"));
            return updatedFulfillment;
        } catch (Exception e) {
            String errorMessage = MessageUtils.updateError("Fulfillment");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public void delete(String id) {
        try {
            Fulfillment fulfillment = getById(id);
            if (fulfillment == null) {
                throw new ServiceException("Fulfillment not found");
            }
            fulfillmentRepository.deleteById(id);
            log.info(MessageUtils.deleteSuccess("Fulfillment"));
        } catch (Exception e) {
            String errorMessage = MessageUtils.deleteError("Fulfillment");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }
} 
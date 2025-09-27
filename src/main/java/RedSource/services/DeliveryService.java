package RedSource.services;

import RedSource.entities.Delivery;
import RedSource.entities.utils.MessageUtils;
import RedSource.exceptions.ServiceException;
import RedSource.repositories.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);
    public static final String DELIVERY = "Delivery";
    public static final String DELIVERIES = "Deliveries";

    private final DeliveryRepository deliveryRepository;

    public List<Delivery> getAll() {
        try {
            List<Delivery> deliveries = deliveryRepository.findAll();
            log.info(MessageUtils.retrieveSuccess(DELIVERIES));
            return deliveries;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(DELIVERIES);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public Delivery getById(String id) {
        try {
            if (Objects.isNull(id)) {
                return null;
            }
            return deliveryRepository.findById(id).orElse(null);
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(DELIVERY);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public Delivery save(Delivery delivery) {
        try {
            delivery.setCreatedAt(new Date());
            delivery.setUpdatedAt(new Date());
            Delivery savedDelivery = deliveryRepository.save(delivery);
            log.info(MessageUtils.saveSuccess(DELIVERY));
            return savedDelivery;
        } catch (Exception e) {
            String errorMessage = MessageUtils.saveError(DELIVERY);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public Delivery update(String id, Delivery delivery) {
        try {
            Delivery existingDelivery = getById(id);
            if (existingDelivery == null) {
                throw new ServiceException("Delivery not found");
            }
            delivery.setId(id);
            delivery.setCreatedAt(existingDelivery.getCreatedAt());
            delivery.setUpdatedAt(new Date());
            Delivery updatedDelivery = deliveryRepository.save(delivery);
            log.info(MessageUtils.updateSuccess(DELIVERY));
            return updatedDelivery;
        } catch (Exception e) {
            String errorMessage = MessageUtils.updateError(DELIVERY);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public void delete(String id) {
        try {
            Delivery delivery = getById(id);
            if (delivery == null) {
                throw new ServiceException("Delivery not found");
            }
            deliveryRepository.deleteById(id);
            log.info(MessageUtils.deleteSuccess(DELIVERY));
        } catch (Exception e) {
            String errorMessage = MessageUtils.deleteError(DELIVERY);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public List<Delivery> findByRequestId(String requestId) {
        try {
            List<Delivery> deliveries = deliveryRepository.findByRequestId(requestId);
            log.info(MessageUtils.retrieveSuccess(DELIVERIES));
            return deliveries;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(DELIVERIES);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public List<Delivery> findAllByStatus(String status) {
        try {
            List<Delivery> deliveries = deliveryRepository.findAllByStatus(status);
            log.info(MessageUtils.retrieveSuccess(DELIVERIES));
            return deliveries;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(DELIVERIES);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public List<Delivery> getDeliveriesByRequestIds(List<String> requestIds) {
        try {
            List<Delivery> deliveries = deliveryRepository.findByRequestIdIn(requestIds);
            log.info(MessageUtils.retrieveSuccess(DELIVERIES));
            return deliveries;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(DELIVERIES);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }
}

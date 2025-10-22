package RedSource.services;

import RedSource.entities.Delivery;
import RedSource.entities.HospitalRequest;
import RedSource.entities.BloodBankUser;
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
    private final HospitalRequestService hospitalRequestService;
    private final BloodBankService bloodBankService;

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

            // ENHANCED AUTO-LINKING: Link deliveries to blood bank and hospital request
            // data
            if (delivery.getRequestId() == null || delivery.getRequestId().isEmpty()
                    || delivery.getRequestId().equals("N/A")) {

                // Try to find a hospital request that matches this delivery context
                try {
                    List<HospitalRequest> requests = hospitalRequestService.getAll();

                    // For now, link to the first available request (in production, this should be
                    // based on delivery context)
                    if (!requests.isEmpty()) {
                        HospitalRequest request = requests.get(0); // Use first available request

                        // Link to this request
                        delivery.setRequestId(request.getId());
                        delivery.setHospitalName(request.getHospitalName());

                        // Get REAL blood bank data from BloodBankUser entity
                        if (request.getBloodBankId() != null) {
                            try {
                                BloodBankUser bloodBank = bloodBankService.getById(request.getBloodBankId());
                                if (bloodBank != null) {
                                    delivery.setBloodBankName(bloodBank.getBloodBankName());
                                    delivery.setBloodBankAddress(bloodBank.getAddress());
                                    delivery.setBloodBankPhone(bloodBank.getPhone());
                                    delivery.setBloodBankEmail(bloodBank.getEmail());
                                    delivery.setContactInfo(bloodBank.getPhone());
                                    log.info("Linked delivery to real blood bank: {}", bloodBank.getBloodBankName());
                                }
                            } catch (Exception e) {
                                log.warn("Could not fetch blood bank details: {}", e.getMessage());
                                // Fallback to request data
                                delivery.setBloodBankName(request.getBloodBankName());
                                delivery.setBloodBankAddress(request.getBloodBankAddress());
                                delivery.setBloodBankPhone(request.getBloodBankPhone());
                                delivery.setBloodBankEmail(request.getBloodBankEmail());
                                delivery.setContactInfo(request.getContactInformation());
                            }
                        }

                        // Set blood items from request
                        if (delivery.getBloodItems() == null || delivery.getBloodItems().isEmpty()) {
                            delivery.setBloodItems(request.getBloodItems());
                        }

                        // Create items summary from blood items
                        if (delivery.getItemsSummary() == null || delivery.getItemsSummary().isEmpty()) {
                            if (request.getBloodItems() != null && !request.getBloodItems().isEmpty()) {
                                String summary = request.getBloodItems().stream()
                                        .map(item -> item.getBloodType() + " (" + item.getUnits() + " units)")
                                        .collect(java.util.stream.Collectors.joining(", "));
                                delivery.setItemsSummary(summary);
                            }
                        }

                        log.info("Auto-linked delivery to hospital request: {}", request.getId());
                    }
                } catch (Exception e) {
                    log.warn("Could not auto-link delivery to hospital request: {}", e.getMessage());
                }
            }

            // FALLBACK: If still missing critical data, populate with defaults
            if (delivery.getHospitalName() == null || delivery.getHospitalName().isEmpty()) {
                delivery.setHospitalName("Unknown Hospital");
            }
            if (delivery.getBloodBankName() == null || delivery.getBloodBankName().isEmpty()) {
                delivery.setBloodBankName("Central Blood Bank");
            }
            if (delivery.getBloodBankAddress() == null || delivery.getBloodBankAddress().isEmpty()) {
                delivery.setBloodBankAddress("Address not available");
            }
            if (delivery.getItemsSummary() == null || delivery.getItemsSummary().isEmpty()) {
                delivery.setItemsSummary("Items not specified");
            }
            if (delivery.getStatus() == null || delivery.getStatus().isEmpty()) {
                delivery.setStatus("PENDING");
            }

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

            // Preserve existing data and only update provided fields
            delivery.setId(id);
            delivery.setCreatedAt(existingDelivery.getCreatedAt());
            delivery.setUpdatedAt(new Date());

            // Preserve critical data if not provided in update
            if (delivery.getRequestId() == null || delivery.getRequestId().isEmpty()) {
                delivery.setRequestId(existingDelivery.getRequestId());
            }
            if (delivery.getHospitalName() == null || delivery.getHospitalName().isEmpty()) {
                delivery.setHospitalName(existingDelivery.getHospitalName());
            }
            if (delivery.getBloodBankName() == null || delivery.getBloodBankName().isEmpty()) {
                delivery.setBloodBankName(existingDelivery.getBloodBankName());
            }
            if (delivery.getBloodBankAddress() == null || delivery.getBloodBankAddress().isEmpty()) {
                delivery.setBloodBankAddress(existingDelivery.getBloodBankAddress());
            }
            if (delivery.getItemsSummary() == null || delivery.getItemsSummary().isEmpty()) {
                delivery.setItemsSummary(existingDelivery.getItemsSummary());
            }
            if (delivery.getBloodItems() == null || delivery.getBloodItems().isEmpty()) {
                delivery.setBloodItems(existingDelivery.getBloodItems());
            }
            if (delivery.getContactInfo() == null || delivery.getContactInfo().isEmpty()) {
                delivery.setContactInfo(existingDelivery.getContactInfo());
            }
            if (delivery.getBloodBankPhone() == null || delivery.getBloodBankPhone().isEmpty()) {
                delivery.setBloodBankPhone(existingDelivery.getBloodBankPhone());
            }
            if (delivery.getBloodBankEmail() == null || delivery.getBloodBankEmail().isEmpty()) {
                delivery.setBloodBankEmail(existingDelivery.getBloodBankEmail());
            }
            if (delivery.getScheduledDate() == null) {
                delivery.setScheduledDate(existingDelivery.getScheduledDate());
            }
            if (delivery.getEstimatedTime() == null || delivery.getEstimatedTime().isEmpty()) {
                delivery.setEstimatedTime(existingDelivery.getEstimatedTime());
            }

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

    public List<Delivery> searchDeliveries(String searchTerm, String status) {
        try {
            List<Delivery> allDeliveries = deliveryRepository.findAll();

            // Filter by status if provided
            if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("all")) {
                allDeliveries = allDeliveries.stream()
                        .filter(delivery -> status.equalsIgnoreCase(delivery.getStatus()))
                        .collect(java.util.stream.Collectors.toList());
            }

            // Filter by search term if provided
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String lowerSearchTerm = searchTerm.toLowerCase();
                allDeliveries = allDeliveries.stream()
                        .filter(delivery -> (delivery.getId() != null
                                && delivery.getId().toLowerCase().contains(lowerSearchTerm)) ||
                                (delivery.getHospitalName() != null
                                        && delivery.getHospitalName().toLowerCase().contains(lowerSearchTerm))
                                ||
                                (delivery.getBloodBankName() != null
                                        && delivery.getBloodBankName().toLowerCase().contains(lowerSearchTerm))
                                ||
                                (delivery.getItemsSummary() != null
                                        && delivery.getItemsSummary().toLowerCase().contains(lowerSearchTerm))
                                ||
                                (delivery.getDriverName() != null
                                        && delivery.getDriverName().toLowerCase().contains(lowerSearchTerm)))
                        .collect(java.util.stream.Collectors.toList());
            }

            log.info(MessageUtils.retrieveSuccess(DELIVERIES));
            return allDeliveries;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(DELIVERIES);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }
}

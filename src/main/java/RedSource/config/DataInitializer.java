package RedSource.config;

import RedSource.entities.*;
import RedSource.repositories.BloodBankRepository;
import RedSource.repositories.DeliveryRepository;
import RedSource.repositories.HospitalRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Profile({"dev", "default"})
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final BloodBankRepository bloodBankRepository;
    private final HospitalRequestRepository hospitalRequestRepository;
    private final DeliveryRepository deliveryRepository;
    private final Environment env;

    @Autowired
    public DataInitializer(BloodBankRepository bloodBankRepository,
                         HospitalRequestRepository hospitalRequestRepository,
                         DeliveryRepository deliveryRepository,
                         Environment env) {
        this.bloodBankRepository = bloodBankRepository;
        this.hospitalRequestRepository = hospitalRequestRepository;
        this.deliveryRepository = deliveryRepository;
        this.env = env;
    }

    @Override
    public void run(String... args) {
        log.info("Running DataInitializer...");
        log.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));
        
        try {
            // Clear existing test data
            log.info("Clearing existing test data...");
            hospitalRequestRepository.deleteAll();
            deliveryRepository.deleteAll();
            bloodBankRepository.deleteAll();
            
            // Create test data
            log.info("Creating test data...");
            createTestData();
            log.info("Test data initialization completed successfully");
        } catch (Exception e) {
            log.error("Error during data initialization: {}", e.getMessage(), e);
        }
    }
    
    private void createTestData() {
        try {
            // Create blood bank
            BloodBank bloodBank = new BloodBank();
            bloodBank.setName("RedSource Blood Center");
            bloodBank.setAddress("123 Main St, City");
            bloodBank.setContactInformation("RedSource Blood Center, 24/7 Hotline");
            bloodBank.setPhone("+1234567890");
            bloodBank.setEmail("info@redsource.ph");
            bloodBank.setOperatingHours("24/7");
            bloodBank.setCreatedAt(new Date());
            bloodBank.setUpdatedAt(new Date());

            // Add inventory
            List<BloodInventoryItem> inventory = new ArrayList<>();
            inventory.add(createBloodInventoryItem("A+", 50));
            inventory.add(createBloodInventoryItem("A-", 30));
            inventory.add(createBloodInventoryItem("B+", 45));
            inventory.add(createBloodInventoryItem("B-", 25));
            inventory.add(createBloodInventoryItem("AB+", 20));
            inventory.add(createBloodInventoryItem("AB-", 15));
            inventory.add(createBloodInventoryItem("O+", 60));
            inventory.add(createBloodInventoryItem("O-", 40));
            bloodBank.setInventory(inventory);
            bloodBank = bloodBankRepository.save(bloodBank);

            // Create test requests and deliveries with different statuses
            createTestDelivery(bloodBank, "PENDING", Arrays.asList("A+", "B-"), Arrays.asList(5, 2), "Emergency delivery for trauma case");
            createTestDelivery(bloodBank, "PROCESSING", Collections.singletonList("O+"), Collections.singletonList(10), "Scheduled delivery for routine transfusion");
            createTestDelivery(bloodBank, "IN TRANSIT", Arrays.asList("A-", "B+"), Arrays.asList(4, 3), "Regular delivery in progress");
            createTestDelivery(bloodBank, "FULFILLED", Arrays.asList("AB+", "O-"), Arrays.asList(3, 2), "Successfully delivered");
            createTestDelivery(bloodBank, "CANCELLED", Collections.singletonList("B+"), Collections.singletonList(5), "Order cancelled by hospital");
            createTestDelivery(bloodBank, "DELAYED", Arrays.asList("O+", "A+"), Arrays.asList(8, 4), "Delivery delayed due to vehicle issues");

        } catch (Exception e) {
            log.error("Error creating test data: {}", e.getMessage(), e);
        }
    }

    private void createTestDelivery(BloodBank bloodBank, String status, List<String> bloodTypes, List<Integer> units, String notes) {
        // Create blood items
        List<BloodInventoryItem> items = new ArrayList<>();
        for (int i = 0; i < bloodTypes.size(); i++) {
            items.add(createBloodInventoryItem(bloodTypes.get(i), units.get(i)));
        }

        // Create hospital request
        HospitalRequest request = new HospitalRequest();
        request.setHospitalId("68c6d2f80df49836c445f26c"); // Default hospital ID
        request.setBloodBankId(bloodBank.getId());
        request.setBloodItems(items);
        request.setStatus(status);
        request.setHospitalName("Sample Hospital");
        request.setHospitalAddress("456 Hospital St, City");
        request.setContactInformation("Hospital Reception, +639876543210");
        request.setNotes(notes);
        request.setCreatedAt(new Date());
        request.setUpdatedAt(new Date());
        request = hospitalRequestRepository.save(request);

        // Create delivery
        Delivery delivery = new Delivery();
        delivery.setRequestId(request.getId());
        delivery.setBloodBankName(bloodBank.getName());
        delivery.setBloodBankAddress(bloodBank.getAddress());
        delivery.setContactInfo(bloodBank.getContactInformation());
        delivery.setBloodBankPhone(bloodBank.getPhone());
        delivery.setBloodBankEmail(bloodBank.getEmail());
        
        // Set items summary
        String summary = items.stream()
            .map(item -> item.getUnits() + " units " + item.getBloodType())
            .collect(Collectors.joining(", "));
        delivery.setItemsSummary(summary);
        
        // Set blood items
        delivery.setBloodItems(new ArrayList<>(items));
        
        // Set dates
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        delivery.setScheduledDate(cal.getTime());
        delivery.setEstimatedTime("10:00 AM - 2:00 PM");
        
        // Set status and priority
        delivery.setStatus(status);
        delivery.setPriority(status.equals("PENDING") || status.equals("IN TRANSIT") || status.equals("DELAYED") ? "HIGH" : "MEDIUM");
        
        // Set driver and vehicle info for in-transit or completed deliveries
        if (status.equals("IN TRANSIT") || status.equals("COMPLETED") || status.equals("DELAYED")) {
            delivery.setDriverName("Juan Dela Cruz");
            delivery.setDriverContact("+639123456789");
            delivery.setVehicleId("ABC-1234");
        }
        
        // Set delivered date and time for fulfilled deliveries
        if (status.equals("FULFILLED")) {
            cal.add(Calendar.HOUR_OF_DAY, -2);
            delivery.setDeliveredDate(cal.getTime());
            delivery.setDeliveredTime("10:30 AM");
        }
        
        // Add tracking history
        delivery.setTrackingHistory(createTrackingHistory(status, now));
        delivery.setCreatedAt(now);
        delivery.setUpdatedAt(now);
        
        deliveryRepository.save(delivery);
    }
    
    private List<TrackingEvent> createTrackingHistory(String status, Date now) {
        List<TrackingEvent> history = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        
        history.add(createTrackingEvent("Request received", now));
        
        if (!status.equals("PENDING")) {
            cal.add(Calendar.MINUTE, 15);
            history.add(createTrackingEvent("Order processed", cal.getTime()));
            
            if (status.equals("IN TRANSIT") || status.equals("FULFILLED") || status.equals("DELAYED")) {
                cal.add(Calendar.HOUR, 1);
                history.add(createTrackingEvent("Picked up by driver", cal.getTime()));
                
                if (status.equals("DELAYED")) {
                    cal.add(Calendar.MINUTE, 30);
                    history.add(createTrackingEvent("Delivery delayed - Vehicle issues", cal.getTime()));
                } else if (status.equals("FULFILLED")) {
                    cal.add(Calendar.HOUR, 1);
                    history.add(createTrackingEvent("Out for delivery", cal.getTime()));
                    cal.add(Calendar.MINUTE, 30);
                    history.add(createTrackingEvent("Delivered", cal.getTime()));
                }
            }
        }
        
        if (status.equals("CANCELLED")) {
            cal.setTime(now);
            cal.add(Calendar.MINUTE, 30);
            history.add(createTrackingEvent("Order cancelled by hospital", cal.getTime()));
        }
        
        return history;
    }

    private BloodInventoryItem createBloodInventoryItem(String bloodType, int units) {
        BloodInventoryItem item = new BloodInventoryItem();
        item.setBloodType(bloodType);
        item.setUnits(units);
        return item;
    }
    
    private TrackingEvent createTrackingEvent(String status, Date timestamp) {
        TrackingEvent event = new TrackingEvent();
        event.setStatus(status);
        event.setTimestamp(timestamp);
        event.setLocation("123 Main St, City");
        event.setNote(status);
        return event;
    }
}

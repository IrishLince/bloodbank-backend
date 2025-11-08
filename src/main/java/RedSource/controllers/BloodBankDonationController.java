package RedSource.controllers;

import RedSource.entities.BloodBankUser;
import RedSource.repositories.BloodBankUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/bloodbanks")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BloodBankDonationController {

    private static final Logger logger = LoggerFactory.getLogger(BloodBankDonationController.class);

    @Autowired
    private BloodBankUserRepository bloodBankUserRepository;

    // Get all blood bank users as donation centers
    @GetMapping("/donation-centers")
    public ResponseEntity<List<BloodBankUser>> getAllDonationCenters() {
        logger.debug("GET /api/bloodbanks/donation-centers - Retrieving all donation centers");
        try {
            List<BloodBankUser> bloodBanks = bloodBankUserRepository.findAll();
            logger.info("GET /api/bloodbanks/donation-centers - Successfully retrieved {} donation centers", bloodBanks.size());
            if (logger.isDebugEnabled()) {
                for (int i = 0; i < bloodBanks.size(); i++) {
                    BloodBankUser bb = bloodBanks.get(i);
                    logger.debug("  {}. {} (ID: {})", i+1, bb.getBloodBankName(), bb.getId());
                }
            }
            return ResponseEntity.ok(bloodBanks);
        } catch (Exception e) {
            logger.error("GET /api/bloodbanks/donation-centers - Error retrieving donation centers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Debug endpoint: Get raw count of blood banks in database
    @GetMapping("/debug/count")
    @CrossOrigin(origins = "*", allowCredentials = "false")
    public ResponseEntity<String> getBloodBankCount() {
        logger.debug("GET /api/bloodbanks/debug/count - Retrieving blood bank count");
        try {
            long count = bloodBankUserRepository.count();
            List<BloodBankUser> allBanks = bloodBankUserRepository.findAll();
            
            logger.info("GET /api/bloodbanks/debug/count - Total count: {}, Retrieved: {}", count, allBanks.size());
            
            StringBuilder response = new StringBuilder();
            response.append("📊 BLOOD BANK DATABASE DEBUG:\n");
            response.append("Total Count: ").append(count).append("\n");
            response.append("Retrieved Count: ").append(allBanks.size()).append("\n\n");
            response.append("📋 All Blood Banks:\n");
            
            for (int i = 0; i < allBanks.size(); i++) {
                BloodBankUser bb = allBanks.get(i);
                response.append(String.format("%d. %s (ID: %s, Email: %s)\n", 
                    i+1, bb.getBloodBankName(), bb.getId(), bb.getEmail()));
            }
            
            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            logger.error("GET /api/bloodbanks/debug/count - Error retrieving blood bank count: {}", e.getMessage(), e);
            String error = "❌ ERROR in debug count: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get blood banks near a specific location
    @GetMapping("/nearby")
    public ResponseEntity<List<BloodBankUser>> getNearbyBloodBanks(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "50") Double radius) {
        logger.debug("GET /api/bloodbanks/nearby - Retrieving nearby blood banks (lat: {}, lng: {}, radius: {})", lat, lng, radius);
        try {
            // For now, return all blood banks (we'll add coordinate filtering later)
            List<BloodBankUser> bloodBanks = bloodBankUserRepository.findAll();
            logger.info("GET /api/bloodbanks/nearby - Successfully retrieved {} nearby blood banks", bloodBanks.size());
            return ResponseEntity.ok(bloodBanks);
        } catch (Exception e) {
            logger.error("GET /api/bloodbanks/nearby - Error retrieving nearby blood banks: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Search blood banks by name or address
    @GetMapping("/search")
    public ResponseEntity<List<BloodBankUser>> searchBloodBanks(@RequestParam String q) {
        logger.debug("GET /api/bloodbanks/search - Searching blood banks with query: {}", q);
        try {
            List<BloodBankUser> byName = bloodBankUserRepository.findByBloodBankNameContainingIgnoreCase(q);
            List<BloodBankUser> byAddress = bloodBankUserRepository.findByAddressContainingIgnoreCase(q);

            // Combine results (remove duplicates)
            byName.addAll(byAddress);
            List<BloodBankUser> uniqueBloodBanks = byName.stream().distinct().toList();

            logger.info("GET /api/bloodbanks/search - Found {} blood banks matching query: {}", uniqueBloodBanks.size(), q);
            return ResponseEntity.ok(uniqueBloodBanks);
        } catch (Exception e) {
            logger.error("GET /api/bloodbanks/search - Error searching blood banks: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

package RedSource.controllers;

import RedSource.entities.BloodBankUser;
import RedSource.repositories.BloodBankUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bloodbanks")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BloodBankDonationController {

    @Autowired
    private BloodBankUserRepository bloodBankUserRepository;

    // Get all blood bank users as donation centers
    @GetMapping("/donation-centers")
    public ResponseEntity<List<BloodBankUser>> getAllDonationCenters() {
        try {
            List<BloodBankUser> bloodBanks = bloodBankUserRepository.findAll();
            
            // DEBUG: Enhanced API response logging
            System.out.println("📍 DONATION CENTERS API - Returning " + bloodBanks.size() + " blood banks");
            System.out.println("🏥 Blood Bank Names:");
            for (int i = 0; i < bloodBanks.size(); i++) {
                BloodBankUser bb = bloodBanks.get(i);
                System.out.println("  " + (i+1) + ". " + bb.getBloodBankName() + " (ID: " + bb.getId() + ")");
            }
            
            return ResponseEntity.ok(bloodBanks);
        } catch (Exception e) {
            System.err.println("❌ ERROR in donation-centers API: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Debug endpoint: Get raw count of blood banks in database
    @GetMapping("/debug/count")
    @CrossOrigin(origins = "*", allowCredentials = "false")
    public ResponseEntity<String> getBloodBankCount() {
        try {
            long count = bloodBankUserRepository.count();
            List<BloodBankUser> allBanks = bloodBankUserRepository.findAll();
            
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
            
            System.out.println(response.toString());
            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            String error = "❌ ERROR in debug count: " + e.getMessage();
            System.err.println(error);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get blood banks near a specific location
    @GetMapping("/nearby")
    public ResponseEntity<List<BloodBankUser>> getNearbyBloodBanks(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "50") Double radius) {
        try {
            // For now, return all blood banks (we'll add coordinate filtering later)
            List<BloodBankUser> bloodBanks = bloodBankUserRepository.findAll();
            return ResponseEntity.ok(bloodBanks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Search blood banks by name or address
    @GetMapping("/search")
    public ResponseEntity<List<BloodBankUser>> searchBloodBanks(@RequestParam String q) {
        try {
            List<BloodBankUser> byName = bloodBankUserRepository.findByBloodBankNameContainingIgnoreCase(q);
            List<BloodBankUser> byAddress = bloodBankUserRepository.findByAddressContainingIgnoreCase(q);

            // Combine results (remove duplicates)
            byName.addAll(byAddress);
            List<BloodBankUser> uniqueBloodBanks = byName.stream().distinct().toList();

            return ResponseEntity.ok(uniqueBloodBanks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

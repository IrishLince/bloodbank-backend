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
            
            // DEBUG: API response logging (can be removed in production)
            System.out.println("📍 DONATION CENTERS API - Returning " + bloodBanks.size() + " blood banks");
            
            return ResponseEntity.ok(bloodBanks);
        } catch (Exception e) {
            System.err.println("❌ ERROR in donation-centers API: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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

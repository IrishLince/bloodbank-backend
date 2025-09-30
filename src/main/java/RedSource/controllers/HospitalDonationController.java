package RedSource.controllers;

import RedSource.entities.Hospital;
import RedSource.repositories.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class HospitalDonationController {

    @Autowired
    private HospitalRepository hospitalRepository;

    // Get all hospitals that are donation centers
    @GetMapping("/donation-centers")
    public ResponseEntity<List<Hospital>> getAllDonationCenters() {
        try {
            List<Hospital> hospitals = hospitalRepository.findByIsDonationCenterTrue();
            return ResponseEntity.ok(hospitals);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get hospitals near a specific location
    @GetMapping("/nearby")
    public ResponseEntity<List<Hospital>> getNearbyHospitals(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "50") Double radius) {
        try {
            // Calculate coordinate bounds based on radius
            // Approximate: 1 degree latitude ≈ 111 km
            Double latDelta = radius / 111.0;
            Double lngDelta = radius / (111.0 * Math.cos(Math.toRadians(lat)));
            
            Double minLat = lat - latDelta;
            Double maxLat = lat + latDelta;
            Double minLng = lng - lngDelta;
            Double maxLng = lng + lngDelta;
            
            List<Hospital> hospitals = hospitalRepository.findByCoordinatesWithinBoundsAndIsDonationCenterTrue(
                minLat, maxLat, minLng, maxLng);
            return ResponseEntity.ok(hospitals);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Search hospitals by name or address
    @GetMapping("/search")
    public ResponseEntity<List<Hospital>> searchHospitals(@RequestParam String q) {
        try {
            List<Hospital> byName = hospitalRepository.findByHospitalNameContainingIgnoreCaseAndIsDonationCenterTrue(q);
            List<Hospital> byAddress = hospitalRepository.findByAddressContainingIgnoreCaseAndIsDonationCenterTrue(q);
            
            // Combine results (remove duplicates)
            byName.addAll(byAddress);
            List<Hospital> uniqueHospitals = byName.stream().distinct().toList();
            
            return ResponseEntity.ok(uniqueHospitals);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get urgent hospitals
    @GetMapping("/urgent")
    public ResponseEntity<List<Hospital>> getUrgentHospitals() {
        try {
            List<Hospital> hospitals = hospitalRepository.findByUrgentNeedTrueAndIsDonationCenterTrue();
            return ResponseEntity.ok(hospitals);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

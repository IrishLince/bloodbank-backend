package RedSource.controllers;

import RedSource.entities.Hospital;
import RedSource.repositories.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/hospitals")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class HospitalDonationController {

    private static final Logger logger = LoggerFactory.getLogger(HospitalDonationController.class);

    @Autowired
    private HospitalRepository hospitalRepository;

    // Get all hospitals that are donation centers
    @GetMapping("/donation-centers")
    public ResponseEntity<List<Hospital>> getAllDonationCenters() {
        logger.debug("GET /api/hospitals/donation-centers - Retrieving all donation center hospitals");
        try {
            List<Hospital> hospitals = hospitalRepository.findByIsDonationCenterTrue();
            logger.info("GET /api/hospitals/donation-centers - Successfully retrieved {} donation center hospitals", hospitals.size());
            return ResponseEntity.ok(hospitals);
        } catch (Exception e) {
            logger.error("GET /api/hospitals/donation-centers - Error retrieving donation centers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get hospitals near a specific location
    @GetMapping("/nearby")
    public ResponseEntity<List<Hospital>> getNearbyHospitals(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "50") Double radius) {
        logger.debug("GET /api/hospitals/nearby - Retrieving nearby hospitals (lat: {}, lng: {}, radius: {} km)", lat, lng, radius);
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
            logger.info("GET /api/hospitals/nearby - Successfully retrieved {} nearby hospitals", hospitals.size());
            return ResponseEntity.ok(hospitals);
        } catch (Exception e) {
            logger.error("GET /api/hospitals/nearby - Error retrieving nearby hospitals: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Search hospitals by name or address
    @GetMapping("/search")
    public ResponseEntity<List<Hospital>> searchHospitals(@RequestParam String q) {
        logger.debug("GET /api/hospitals/search - Searching hospitals with query: {}", q);
        try {
            List<Hospital> byName = hospitalRepository.findByHospitalNameContainingIgnoreCaseAndIsDonationCenterTrue(q);
            List<Hospital> byAddress = hospitalRepository.findByAddressContainingIgnoreCaseAndIsDonationCenterTrue(q);
            
            // Combine results (remove duplicates)
            byName.addAll(byAddress);
            List<Hospital> uniqueHospitals = byName.stream().distinct().toList();
            
            logger.info("GET /api/hospitals/search - Found {} hospitals matching query: {}", uniqueHospitals.size(), q);
            return ResponseEntity.ok(uniqueHospitals);
        } catch (Exception e) {
            logger.error("GET /api/hospitals/search - Error searching hospitals: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get urgent hospitals
    @GetMapping("/urgent")
    public ResponseEntity<List<Hospital>> getUrgentHospitals() {
        logger.debug("GET /api/hospitals/urgent - Retrieving hospitals with urgent need");
        try {
            List<Hospital> hospitals = hospitalRepository.findByUrgentNeedTrueAndIsDonationCenterTrue();
            logger.info("GET /api/hospitals/urgent - Successfully retrieved {} hospitals with urgent need", hospitals.size());
            return ResponseEntity.ok(hospitals);
        } catch (Exception e) {
            logger.error("GET /api/hospitals/urgent - Error retrieving urgent hospitals: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

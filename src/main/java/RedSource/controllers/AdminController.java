package RedSource.controllers;

import RedSource.entities.BloodBankUser;
import RedSource.entities.Hospital;
import RedSource.entities.enums.UserRoleType;
import RedSource.repositories.BloodBankUserRepository;
import RedSource.repositories.HospitalRepository;
import RedSource.repositories.UserRepository;
import RedSource.entities.utils.ResponseUtils;
import RedSource.services.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private BloodBankUserRepository bloodBankUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Register a new Hospital user
     */
    @PostMapping(value = "/register-hospital", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerHospital(
            @RequestPart("data") String hospitalDataJson,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        try {
            // Parse JSON data
            ObjectMapper objectMapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> hospitalData = (Map<String, Object>) objectMapper.readValue(hospitalDataJson, Map.class);
            
            // Extract data
            String email = (String) hospitalData.get("email");
            String password = (String) hospitalData.get("password");
            String hospitalName = (String) hospitalData.get("hospitalName");
            String licenseNumber = (String) hospitalData.get("licenseNumber");
            String contactNumber = (String) hospitalData.get("contactNumber");
            String address = (String) hospitalData.get("address");
            String operatingHours = (String) hospitalData.get("operatingHours");
            Boolean isDonationCenter = false; // Default to false, checkbox removed from frontend

            // Validate required fields
            if (email == null || password == null || hospitalName == null || licenseNumber == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                "Missing required fields: email, password, hospitalName, licenseNumber"
                        )
                );
            }

            // Check if email already exists in User collection (for cross-collection uniqueness)
            if (userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.CONFLICT,
                                "Email already exists in users collection"
                        )
                );
            }

            // Check if email already exists in Hospital collection
            if (hospitalRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.CONFLICT,
                                "Email already exists as a hospital"
                        )
                );
            }

            // Check if email already exists in BloodBankUser collection (for cross-collection uniqueness)
            if (bloodBankUserRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.CONFLICT,
                                "Email already exists as a blood bank"
                        )
                );
            }

            // Note: License number uniqueness check can be added if needed

            // Only create Hospital entity (NOT User entity in users collection)
            Hospital savedHospital;
            String photoUrl = null; // Declare outside try block for cleanup in catch
            
            try {
                // Store photo if provided
                if (photo != null && !photo.isEmpty()) {
                    try {
                        photoUrl = fileStorageService.storeUserPhoto(photo);
                    } catch (Exception ex) {
                        // If photo upload fails, proceed without photo
                        System.err.println("Failed to upload hospital profile photo: " + ex.getMessage());
                        photoUrl = null;
                    }
                }
                
                // Create Hospital entity only
                Hospital.HospitalBuilder hospitalBuilder = Hospital.builder()
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .username(email)
                        .hospitalName(hospitalName)
                        .licenseNumber(licenseNumber)
                        .phone(contactNumber)
                        .address(address)
                        .operatingHours(operatingHours)
                        .isDonationCenter(isDonationCenter)
                        .createdAt(new Date())
                        .updatedAt(new Date());
                
                if (photoUrl != null) {
                    hospitalBuilder.profilePhotoUrl(photoUrl);
                }
                
                Hospital hospital = hospitalBuilder.build();

                savedHospital = hospitalRepository.save(hospital);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // If registration fails, clean up uploaded photo
                if (photoUrl != null) {
                    try {
                        fileStorageService.deleteFile(photoUrl);
                    } catch (Exception deleteEx) {
                        System.err.println("Failed to clean up photo after registration error: " + deleteEx.getMessage());
                    }
                }
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.CONFLICT,
                                "Email already exists (duplicate key detected)"
                        )
                );
            }
            
            // If save succeeds but there was a photo upload error, we already handled it above

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("hospitalId", savedHospital.getId()); // Use Hospital entity's MongoDB ObjectId
            response.put("email", savedHospital.getEmail());
            response.put("hospitalName", savedHospital.getHospitalName());
            response.put("role", "HOSPITAL");

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.CREATED,
                            "Hospital registered successfully",
                            response
                    )
            );

        } catch (Exception e) {
            // If any error occurs and photo was uploaded, clean it up
            // Note: photoUrl is scoped within the try block, so we can't access it here
            // The photo cleanup is handled in the DuplicateKeyException catch block above
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to register hospital: " + e.getMessage()
                    )
            );
        }
    }

    /**
     * Register a new Blood Bank user
     */
    @PostMapping(value = "/register-bloodbank", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerBloodBank(
            @RequestPart("data") String bloodBankDataJson,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        try {
            // Parse JSON data
            ObjectMapper objectMapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> bloodBankData = (Map<String, Object>) objectMapper.readValue(bloodBankDataJson, Map.class);
            
            // Extract data
            String email = (String) bloodBankData.get("email");
            String password = (String) bloodBankData.get("password");
            String bloodBankName = (String) bloodBankData.get("bloodBankName");
            String licenseNumber = (String) bloodBankData.get("licenseNumber");
            String contactNumber = (String) bloodBankData.get("contactNumber");
            String address = (String) bloodBankData.get("address");
            
            // Handle operating days and hours
            @SuppressWarnings("unchecked")
            List<String> operatingDays = (List<String>) bloodBankData.get("operatingDays");
            String openingTime = (String) bloodBankData.get("openingTime");
            String closingTime = (String) bloodBankData.get("closingTime");
            String operatingHours = constructOperatingHours(operatingDays, openingTime, closingTime);

            // Validate required fields
            if (email == null || password == null || bloodBankName == null || licenseNumber == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                "Missing required fields: email, password, bloodBankName, licenseNumber"
                        )
                );
            }

            // Check if email already exists in User collection (for cross-collection uniqueness)
            if (userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.CONFLICT,
                                "Email already exists in users collection"
                        )
                );
            }

            // Check if email already exists in Hospital collection (for cross-collection uniqueness)
            if (hospitalRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.CONFLICT,
                                "Email already exists as a hospital"
                        )
                );
            }

            // Check if email already exists in BloodBankUser collection
            if (bloodBankUserRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.CONFLICT,
                                "Email already exists as a blood bank"
                        )
                );
            }

            // Note: License number uniqueness check can be added if needed

            // Only create BloodBankUser entity (NOT User entity in users collection)
            BloodBankUser savedBloodBank;
            String photoUrl = null; // Declare outside try block for cleanup in catch
            
            try {
                // Store photo if provided
                if (photo != null && !photo.isEmpty()) {
                    try {
                        photoUrl = fileStorageService.storeUserPhoto(photo);
                    } catch (Exception ex) {
                        // If photo upload fails, proceed without photo
                        System.err.println("Failed to upload blood bank profile photo: " + ex.getMessage());
                        photoUrl = null;
                    }
                }
                
                // Create BloodBankUser entity only
                BloodBankUser.BloodBankUserBuilder bloodBankUserBuilder = BloodBankUser.builder()
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .username(email)
                        .bloodBankName(bloodBankName)
                        .licenseNumber(licenseNumber)
                        .phone(contactNumber)
                        .address(address)
                        .operatingHours(operatingHours)
                        .role("BLOODBANK")
                        .createdAt(new Date())
                        .updatedAt(new Date());
                
                if (photoUrl != null) {
                    bloodBankUserBuilder.profilePhotoUrl(photoUrl);
                }
                
                BloodBankUser bloodBankUser = bloodBankUserBuilder.build();

                savedBloodBank = bloodBankUserRepository.save(bloodBankUser);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // If registration fails, clean up uploaded photo
                if (photoUrl != null) {
                    try {
                        fileStorageService.deleteFile(photoUrl);
                    } catch (Exception deleteEx) {
                        System.err.println("Failed to clean up photo after registration error: " + deleteEx.getMessage());
                    }
                }
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.CONFLICT,
                                "Email already exists (duplicate key detected)"
                        )
                );
            }
            
            // If save succeeds but there was a photo upload error, we already handled it above

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("bloodBankId", savedBloodBank.getId()); // Use BloodBankUser entity's MongoDB ObjectId
            response.put("email", savedBloodBank.getEmail());
            response.put("bloodBankName", savedBloodBank.getBloodBankName());
            response.put("role", "BLOODBANK");

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.CREATED,
                            "Blood Bank registered successfully",
                            response
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to register blood bank: " + e.getMessage()
                    )
            );
        }
    }

    /**
     * Get all hospitals
     */
    @GetMapping("/hospitals")
    public ResponseEntity<?> getAllHospitals() {
        try {
            List<Hospital> hospitals = hospitalRepository.findAll();
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Hospitals retrieved successfully",
                            hospitals
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to retrieve hospitals: " + e.getMessage()
                    )
            );
        }
    }

    /**
     * Get all blood banks
     */
    @GetMapping("/bloodbanks")
    public ResponseEntity<?> getAllBloodBanks() {
        try {
            List<BloodBankUser> bloodBanks = bloodBankUserRepository.findAll();
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Blood banks retrieved successfully",
                            bloodBanks
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to retrieve blood banks: " + e.getMessage()
                    )
            );
        }
    }

    /**
     * Update hospital information
     */
    @PutMapping("/hospitals/{id}")
    public ResponseEntity<?> updateHospital(@PathVariable String id, @RequestBody Map<String, Object> hospitalData) {
        try {
            // Find existing hospital
            Hospital hospital = hospitalRepository.findById(id).orElse(null);
            if (hospital == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Hospital not found"
                        )
                );
            }

            // Update fields if provided
            if (hospitalData.containsKey("hospitalName")) {
                hospital.setHospitalName((String) hospitalData.get("hospitalName"));
            }
            if (hospitalData.containsKey("email")) {
                hospital.setEmail((String) hospitalData.get("email"));
            }
            if (hospitalData.containsKey("phone")) {
                hospital.setPhone((String) hospitalData.get("phone"));
            }
            if (hospitalData.containsKey("licenseNumber")) {
                hospital.setLicenseNumber((String) hospitalData.get("licenseNumber"));
            }
            if (hospitalData.containsKey("address")) {
                hospital.setAddress((String) hospitalData.get("address"));
            }
            if (hospitalData.containsKey("operatingHours")) {
                hospital.setOperatingHours((String) hospitalData.get("operatingHours"));
            }
            if (hospitalData.containsKey("isDonationCenter")) {
                hospital.setIsDonationCenter((Boolean) hospitalData.get("isDonationCenter"));
            }

            hospital.setUpdatedAt(new Date());
            Hospital updatedHospital = hospitalRepository.save(hospital);

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Hospital updated successfully",
                            updatedHospital
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to update hospital: " + e.getMessage()
                    )
            );
        }
    }

    /**
     * Update blood bank information
     */
    @PutMapping("/bloodbanks/{id}")
    public ResponseEntity<?> updateBloodBank(@PathVariable String id, @RequestBody Map<String, Object> bloodBankData) {
        try {
            // Find existing blood bank
            BloodBankUser bloodBank = bloodBankUserRepository.findById(id).orElse(null);
            if (bloodBank == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Blood bank not found"
                        )
                );
            }

            // Update fields if provided
            if (bloodBankData.containsKey("bloodBankName")) {
                bloodBank.setBloodBankName((String) bloodBankData.get("bloodBankName"));
            }
            if (bloodBankData.containsKey("email")) {
                bloodBank.setEmail((String) bloodBankData.get("email"));
            }
            if (bloodBankData.containsKey("phone")) {
                bloodBank.setPhone((String) bloodBankData.get("phone"));
            }
            if (bloodBankData.containsKey("licenseNumber")) {
                bloodBank.setLicenseNumber((String) bloodBankData.get("licenseNumber"));
            }
            if (bloodBankData.containsKey("address")) {
                bloodBank.setAddress((String) bloodBankData.get("address"));
            }
            
            // Handle operating hours update - prioritize individual components for consistency
            if (bloodBankData.containsKey("operatingDays") || bloodBankData.containsKey("openingTime") || bloodBankData.containsKey("closingTime")) {
                // New format with operating days - construct using backend logic for consistency
                @SuppressWarnings("unchecked")
                List<String> operatingDays = (List<String>) bloodBankData.get("operatingDays");
                String openingTime = (String) bloodBankData.get("openingTime");
                String closingTime = (String) bloodBankData.get("closingTime");
                String newOperatingHours = constructOperatingHours(operatingDays, openingTime, closingTime);
                
                // DEBUG: Backend update logging (can be removed in production)
                System.out.println("🔧 BACKEND UPDATE - Operating hours: " + newOperatingHours);
                
                bloodBank.setOperatingHours(newOperatingHours);
                
            } else if (bloodBankData.containsKey("operatingHours")) {
                // Fallback to direct operating hours string (legacy format)
                String directHours = (String) bloodBankData.get("operatingHours");
                System.out.println("🔧 BACKEND UPDATE - Direct hours: " + directHours);
                bloodBank.setOperatingHours(directHours);
            }

            bloodBank.setUpdatedAt(new Date());
            BloodBankUser updatedBloodBank = bloodBankUserRepository.save(bloodBank);

            // DEBUG: Confirm save operation (can be removed in production)
            System.out.println("✅ Blood bank updated: " + updatedBloodBank.getBloodBankName() + " | Hours: " + updatedBloodBank.getOperatingHours());

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Blood bank updated successfully",
                            updatedBloodBank
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to update blood bank: " + e.getMessage()
                    )
            );
        }
    }

    /**
     * Get dashboard statistics
     */
    @GetMapping("/dashboard-stats")
    public ResponseEntity<?> getDashboardStats() {
        try {
            long totalHospitals = hospitalRepository.count();
            long totalBloodBanks = bloodBankUserRepository.count();
            long totalDonors = userRepository.countByRole(UserRoleType.DONOR);
            long totalUsers = userRepository.count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalHospitals", totalHospitals);
            stats.put("totalBloodBanks", totalBloodBanks);
            stats.put("totalDonors", totalDonors);
            stats.put("totalUsers", totalUsers);

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Dashboard statistics retrieved successfully",
                            stats
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to retrieve dashboard statistics: " + e.getMessage()
                    )
            );
        }
    }
    
    /**
     * Construct operating hours string in the format expected by frontend filtering
     * e.g., "Mon-Fri 09:00 - 17:00" or "Mon,Wed,Fri 08:00 - 18:00"
     */
    private String constructOperatingHours(List<String> operatingDays, String openingTime, String closingTime) {
        // Handle fallback for old format
        if (operatingDays == null || operatingDays.isEmpty()) {
            if (openingTime != null && closingTime != null) {
                return openingTime + " - " + closingTime; // Fallback to old format
            }
            return "09:00 - 17:00"; // Default hours
        }
        
        if (openingTime == null) openingTime = "09:00";
        if (closingTime == null) closingTime = "17:00";
        
        // Normalize time format to ensure HH:MM (double-digit hours and minutes)
        openingTime = normalizeTimeFormat(openingTime);
        closingTime = normalizeTimeFormat(closingTime);
        
        // Sort days in proper order
        String[] dayOrder = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        List<String> sortedDays = new java.util.ArrayList<>();
        for (String day : dayOrder) {
            if (operatingDays.contains(day)) {
                sortedDays.add(day);
            }
        }
        
        if (sortedDays.isEmpty()) {
            return openingTime + " - " + closingTime; // Fallback if no days specified
        }
        
        // Check if days are consecutive for range format (e.g., Mon-Fri)
        String dayRange = formatDayRange(sortedDays, dayOrder);
        
        return dayRange + " " + openingTime + " - " + closingTime;
    }
    
    /**
     * Format day list into range if consecutive, otherwise comma-separated
     */
    private String formatDayRange(List<String> sortedDays, String[] dayOrder) {
        if (sortedDays.size() == 1) {
            return sortedDays.get(0);
        }
        
        // Find indices of days in the order array
        List<Integer> indices = new java.util.ArrayList<>();
        for (String day : sortedDays) {
            for (int i = 0; i < dayOrder.length; i++) {
                if (dayOrder[i].equals(day)) {
                    indices.add(i);
                    break;
                }
            }
        }
        
        // Check if consecutive
        boolean consecutive = true;
        for (int i = 1; i < indices.size(); i++) {
            if (indices.get(i) != indices.get(i-1) + 1) {
                consecutive = false;
                break;
            }
        }
        
        if (consecutive && sortedDays.size() > 2) {
            // Use range format: Mon-Fri
            return sortedDays.get(0) + "-" + sortedDays.get(sortedDays.size() - 1);
        } else {
            // Use comma-separated format: Mon,Wed,Fri
            return String.join(",", sortedDays);
        }
    }
    
    /**
     * Normalize time format to ensure HH:MM (double-digit hours and minutes)
     * e.g., "9:00" -> "09:00", "09:00" -> "09:00"
     */
    private String normalizeTimeFormat(String time) {
        if (time == null || time.trim().isEmpty()) {
            return "09:00";
        }
        
        time = time.trim();
        
        // Check if it's already in proper format
        if (time.matches("\\d{2}:\\d{2}")) {
            return time;
        }
        
        // Handle single-digit hours (e.g., "9:00" -> "09:00")
        if (time.matches("\\d{1}:\\d{2}")) {
            return "0" + time;
        }
        
        // Handle other formats or malformed input
        try {
            String[] parts = time.split(":");
            if (parts.length == 2) {
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                return String.format("%02d:%02d", hours, minutes);
            }
        } catch (NumberFormatException e) {
            // Log error and return default
            System.err.println("Could not parse time format: " + time + ". Using default 09:00");
        }
        
        return "09:00"; // Fallback default
    }
}
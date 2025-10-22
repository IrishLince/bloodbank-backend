package RedSource.controllers;

import RedSource.entities.Hospital;
import RedSource.entities.PointTransaction;
import RedSource.entities.RewardRedemption;
import RedSource.entities.RewardPoints;
import RedSource.entities.User;
import RedSource.entities.utils.MessageUtils;
import RedSource.entities.utils.ResponseUtils;
import RedSource.services.RewardPointsService;
import RedSource.services.RewardPointsManagementService;
import RedSource.services.HospitalService;
import RedSource.repositories.UserRepository;
import RedSource.repositories.RewardRedemptionRepository;
import RedSource.repositories.HospitalRepository;
import RedSource.repositories.BloodBankUserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reward-points")
@RequiredArgsConstructor
public class RewardPointsController {

    public static final String REWARD_POINTS = "Reward Points";
    public static final String REWARD_POINTS_PLURAL = "Reward Points";

        private final RewardPointsService rewardPointsService;
        private final RewardPointsManagementService rewardPointsManagementService;
        private final UserRepository userRepository;
        private final RewardRedemptionRepository rewardRedemptionRepository;
        private final HospitalService hospitalService;
        private final HospitalRepository hospitalRepository;
        private final BloodBankUserRepository bloodBankUserRepository;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.retrieveSuccess(REWARD_POINTS_PLURAL),
                                                rewardPointsService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        RewardPoints rewardPoints = rewardPointsService.getById(id);
        if (rewardPoints == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.NOT_FOUND,
                                                        "Reward points not found"));
        }
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.retrieveSuccess(REWARD_POINTS),
                                                rewardPoints));
    }

    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody RewardPoints rewardPoints, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                                                        MessageUtils.validationErrors(bindingResult)));
        }
        RewardPoints savedRewardPoints = rewardPointsService.save(rewardPoints);
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.saveSuccess(REWARD_POINTS),
                                                savedRewardPoints));
    }

    @PutMapping("/{id}")
        public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody RewardPoints rewardPoints,
                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                                                        MessageUtils.validationErrors(bindingResult)));
        }
        try {
            RewardPoints updatedRewardPoints = rewardPointsService.update(id, rewardPoints);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.updateSuccess(REWARD_POINTS),
                                                        updatedRewardPoints));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.NOT_FOUND,
                                                        e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            rewardPointsService.delete(id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                                                        MessageUtils.deleteSuccess(REWARD_POINTS)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.NOT_FOUND,
                                                        e.getMessage()));
        }
    }

    // New endpoints for reward points management

    /**
     * Get donor's reward points summary
     */
    @GetMapping("/donor/{donorId}/summary")
    public ResponseEntity<?> getDonorSummary(@PathVariable String donorId) {
        try {
            User donor = userRepository.findById(donorId)
                    .orElseThrow(() -> new RuntimeException("Donor not found"));

            Map<String, Object> summary = new HashMap<>();
            summary.put("donorId", donor.getId());
            summary.put("donorName", donor.getName());
            summary.put("rewardPoints", donor.getRewardPoints() != null ? donor.getRewardPoints() : 0);
                        summary.put("totalDonations",
                                        donor.getTotalDonations() != null ? donor.getTotalDonations() : 0);
            summary.put("donorTier", donor.getDonorTier() != null ? donor.getDonorTier() : "NEW");

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Donor reward points summary retrieved successfully",
                                                        summary));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.NOT_FOUND,
                                                        e.getMessage()));
        }
    }

    /**
     * Get donor's point transaction history
     */
    @GetMapping("/donor/{donorId}/history")
    public ResponseEntity<?> getPointHistory(@PathVariable String donorId) {
        try {
            List<PointTransaction> history = rewardPointsManagementService.getPointHistory(donorId);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Point history retrieved successfully",
                                                        history));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                                                        e.getMessage()));
        }
    }

    /**
     * Get donor's redemption history
     */
    @GetMapping("/donor/{donorId}/redemptions")
    public ResponseEntity<?> getRedemptionHistory(@PathVariable String donorId) {
        try {
                        List<Map<String, Object>> redemptions = rewardPointsManagementService
                                        .getRedemptionHistory(donorId);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Redemption history retrieved successfully",
                                                        redemptions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                                                        e.getMessage()));
        }
    }

    /**
     * Redeem a reward
     */
    @PostMapping("/donor/{donorId}/redeem")
    public ResponseEntity<?> redeemReward(
            @PathVariable String donorId,
            @RequestBody Map<String, Object> redeemRequest) {
        try {
            String rewardTitle = (String) redeemRequest.get("rewardTitle");
            String rewardType = (String) redeemRequest.get("rewardType");
            Integer pointsCost = (Integer) redeemRequest.get("pointsCost");
            String tier = (String) redeemRequest.get("tier");
            String redeemableAt = (String) redeemRequest.get("redeemableAt");

            if (rewardTitle == null || rewardType == null || pointsCost == null || tier == null || redeemableAt == null) {
                return ResponseEntity.badRequest().body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                                                "Missing required fields: rewardTitle, rewardType, pointsCost, tier, redeemableAt"));
            }

            RewardRedemption redemption = rewardPointsManagementService.redeemReward(
                    donorId, rewardTitle, rewardType, pointsCost, tier, redeemableAt);

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Reward redeemed successfully",
                                                        redemption));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                                                        e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                                                        "Failed to redeem reward: " + e.getMessage()));
        }
    }

    /**
     * Award donation points (called after successful donation)
     */
    @PostMapping("/donor/{donorId}/award-donation")
    public ResponseEntity<?> awardDonationPoints(
            @PathVariable String donorId,
            @RequestBody Map<String, String> request) {
        try {
            String donationId = request.get("donationId");
            if (donationId == null) {
                return ResponseEntity.badRequest().body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                                                "Missing required field: donationId"));
            }

            User updatedDonor = rewardPointsManagementService.awardDonationPoints(donorId, donationId);

            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Donation points awarded successfully",
                                                        updatedDonor));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                                                        e.getMessage()));
        }
    }

    /**
     * Initialize reward points for a donor
     */
    @PostMapping("/donor/{donorId}/initialize")
    public ResponseEntity<?> initializeRewardPoints(@PathVariable String donorId) {
        try {
            rewardPointsManagementService.initializeRewardPoints(donorId);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                                                        "Reward points initialized successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                                                        e.getMessage()));
        }
    }

    // Blood Bank Voucher Validation Endpoints

    /**
     * Get all blood bag voucher redemptions (for blood bank)
     * Filtered by blood bank ID if provided
     */
    @GetMapping("/bloodbank/vouchers")
    public ResponseEntity<?> getAllBloodBagVouchers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String bloodBankId) {
        try {
            List<RewardRedemption> vouchers;
            
            if (bloodBankId != null && !bloodBankId.isEmpty()) {
                // Filter by blood bank ID (vouchers validated by this blood bank)
                if (status != null && !status.isEmpty()) {
                                        vouchers = rewardPointsManagementService
                                                        .getVouchersByBloodBankAndStatus(bloodBankId, status);
                } else {
                    vouchers = rewardPointsManagementService.getVouchersByBloodBank(bloodBankId);
                }
            } else {
                // Get all vouchers (admin view)
                if (status != null && !status.isEmpty()) {
                    vouchers = rewardPointsManagementService.getBloodBagVouchersByStatus(status);
                } else {
                    vouchers = rewardPointsManagementService.getAllBloodBagVouchers();
                }
            }
            
            // Enhance vouchers with donor information
            List<Map<String, Object>> enhancedVouchers = vouchers.stream().map(voucher -> {
                Map<String, Object> voucherMap = new HashMap<>();
                voucherMap.put("id", voucher.getId());
                voucherMap.put("donorId", voucher.getDonorId());
                voucherMap.put("rewardTitle", voucher.getRewardTitle());
                voucherMap.put("rewardType", voucher.getRewardType());
                voucherMap.put("tier", voucher.getTier());
                voucherMap.put("redeemableAt", voucher.getRedeemableAt());
                voucherMap.put("pointsCost", voucher.getPointsCost());
                voucherMap.put("status", voucher.getStatus());
                voucherMap.put("redeemedDate", voucher.getRedeemedDate());
                voucherMap.put("deliveredDate", voucher.getDeliveredDate());
                voucherMap.put("voucherCode", voucher.getVoucherCode());
                voucherMap.put("expiryDate", voucher.getExpiryDate());
                voucherMap.put("notes", voucher.getNotes());
                voucherMap.put("validatedByBloodBankId", voucher.getValidatedByBloodBankId());
                voucherMap.put("createdAt", voucher.getCreatedAt());
                voucherMap.put("updatedAt", voucher.getUpdatedAt());
                
                // Add donor information
                try {
                    User donor = userRepository.findById(voucher.getDonorId()).orElse(null);
                    if (donor != null) {
                        voucherMap.put("donorName", donor.getName());
                        voucherMap.put("donorEmail", donor.getEmail());
                    } else {
                        voucherMap.put("donorName", "Unknown Donor");
                        voucherMap.put("donorEmail", "N/A");
                    }
                } catch (Exception e) {
                    voucherMap.put("donorName", "Error loading donor");
                    voucherMap.put("donorEmail", "N/A");
                }
                
                return voucherMap;
            }).toList();
            
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Blood bag vouchers retrieved successfully",
                                                        enhancedVouchers));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                                                        e.getMessage()));
        }
    }

    /**
     * Validate a voucher code
     */
    @GetMapping("/bloodbank/vouchers/validate/{voucherCode}")
    public ResponseEntity<?> validateVoucherCodeForBloodBank(@PathVariable String voucherCode) {
        try {
            RewardRedemption voucher = rewardPointsManagementService.validateVoucherCodeForBloodBank(voucherCode);
            
            if (voucher == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                                                "Invalid voucher code"));
            }

            // Get donor information
            User donor = userRepository.findById(voucher.getDonorId()).orElse(null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("voucher", voucher);
            response.put("donorName", donor != null ? donor.getName() : "Unknown");
            response.put("donorEmail", donor != null ? donor.getEmail() : "Unknown");
            response.put("isValid", "PENDING".equals(voucher.getStatus()));
            
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Voucher validated successfully",
                                                        response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                                                        e.getMessage()));
        }
    }

    /**
     * Accept a blood bag voucher (blood bank accepts the request)
     */
    @PostMapping("/bloodbank/vouchers/{voucherId}/accept")
    public ResponseEntity<?> acceptVoucher(
            @PathVariable String voucherId,
            @RequestBody Map<String, String> request) {
        try {
            String bloodBankId = request.get("bloodBankId");
            if (bloodBankId == null || bloodBankId.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                                                "Blood bank ID is required"));
            }
            
            // First validate the voucher to get its type
            RewardRedemption validationResult = rewardPointsManagementService.validateVoucherCodeForBloodBank(
                rewardRedemptionRepository.findById(voucherId)
                    .orElseThrow(() -> new RuntimeException("Voucher not found"))
                    .getVoucherCode()
            );
            
            RewardRedemption voucher;
            if ("BLOOD_BAG_VOUCHER".equals(validationResult.getRewardType())) {
                voucher = rewardPointsManagementService.acceptBloodBagVoucher(voucherId, bloodBankId);
            } else {
                voucher = rewardPointsManagementService.acceptGeneralVoucher(voucherId, bloodBankId);
            }
            
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Voucher accepted successfully",
                                                        voucher));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                                                        e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                                                        e.getMessage()));
        }
    }

    /**
     * Mark voucher as completed (blood bag claimed)
     */
    @PostMapping("/bloodbank/vouchers/{voucherId}/complete")
    public ResponseEntity<?> completeVoucher(@PathVariable String voucherId) {
        try {
            RewardRedemption voucher = rewardPointsManagementService.completeBloodBagVoucher(voucherId);
            
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Voucher marked as completed",
                                                        voucher));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                                                        e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                                                        e.getMessage()));
        }
    }

    /**
     * Reject a blood bag voucher
     */
    @PostMapping("/bloodbank/vouchers/{voucherId}/reject")
    public ResponseEntity<?> rejectVoucher(
            @PathVariable String voucherId,
            @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            String bloodBankId = request.get("bloodBankId");
            RewardRedemption voucher = rewardPointsManagementService.rejectBloodBagVoucher(voucherId, reason, bloodBankId);
            
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Voucher rejected",
                                                        voucher));
                } catch (RuntimeException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                        ResponseUtils.buildErrorResponse(
                                                        HttpStatus.BAD_REQUEST,
                                                        e.getMessage()));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                        ResponseUtils.buildErrorResponse(
                                                        HttpStatus.INTERNAL_SERVER_ERROR,
                                                        e.getMessage()));
                }
        }

        // Hospital-specific endpoints for medical service voucher validation

        /**
         * Validate a medical service voucher (for hospitals)
         */
        @GetMapping("/hospital/vouchers/validate/{voucherCode}")
        public ResponseEntity<?> validateMedicalServiceVoucher(@PathVariable String voucherCode) {
                try {
                        RewardRedemption voucher = rewardPointsManagementService.validateVoucherCodeForHospital(voucherCode);

                        if (voucher == null) {
                                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                                ResponseUtils.buildErrorResponse(
                                                                HttpStatus.NOT_FOUND,
                                                                "Invalid voucher code"));
                        }

                        // Get donor information
                        User donor = userRepository.findById(voucher.getDonorId()).orElse(null);

                        Map<String, Object> response = new HashMap<>();
                        response.put("id", voucher.getId());
                        response.put("voucherCode", voucher.getVoucherCode());
                        response.put("rewardTitle", voucher.getRewardTitle());
                        response.put("rewardType", voucher.getRewardType());
                        response.put("status", voucher.getStatus());
                        response.put("donorId", voucher.getDonorId());
                        response.put("donorName", donor != null ? donor.getName() : "Unknown");
                        response.put("donorEmail", donor != null ? donor.getEmail() : "Unknown");
                        response.put("redeemedDate", voucher.getRedeemedDate());
                        response.put("expiryDate", voucher.getExpiryDate());
                        response.put("notes", voucher.getNotes());
                        response.put("isValid", "PENDING".equals(voucher.getStatus())
                                        || "PROCESSING".equals(voucher.getStatus()));

                        return ResponseEntity.ok(
                                        ResponseUtils.buildSuccessResponse(
                                                        HttpStatus.OK,
                                                        "Voucher validated successfully",
                                                        response));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                        ResponseUtils.buildErrorResponse(
                                                        HttpStatus.INTERNAL_SERVER_ERROR,
                                                        e.getMessage()));
                }
        }

        /**
         * Update medical service voucher status (for hospitals)
         */
        @PostMapping("/hospital/vouchers/{voucherId}/update-status")
        public ResponseEntity<?> updateMedicalServiceVoucherStatus(
                        @PathVariable String voucherId,
                        @RequestBody Map<String, String> request) {
                try {
                        String status = request.get("status");
                        String hospitalId = request.get("hospitalId");
                        String notes = request.get("notes");

                        if (status == null || status.isEmpty()) {
                                return ResponseEntity.badRequest().body(
                                                ResponseUtils.buildErrorResponse(
                                                                HttpStatus.BAD_REQUEST,
                                                                "Status is required"));
                        }

                        RewardRedemption voucher;

                        // Handle cancellation separately to refund points
                        if ("CANCELLED".equals(status)) {
                                String reason = notes != null ? notes : "Cancelled by hospital";
                                if (hospitalId != null && !hospitalId.isEmpty()) {
                                        reason += " [Hospital ID: " + hospitalId + "]";
                                }
                                voucher = rewardPointsManagementService.cancelMedicalServiceVoucher(voucherId, reason);
                        } else {
                                // For other status updates (PROCESSING, COMPLETED, etc.)
                                voucher = rewardPointsManagementService.updateRedemptionStatus(voucherId, status);

                                // Add hospital validation info if provided
                                if (hospitalId != null && !hospitalId.isEmpty()) {
                                        try {
                                                Hospital hospital = hospitalService.getEntityById(hospitalId);
                                                if (hospital != null) {
                                                        voucher.setValidatedByHospitalId(hospitalId);
                                                        voucher.setHospitalName(hospital.getHospitalName());
                                                        voucher.setHospitalAddress(hospital.getAddress());
                                                        voucher.setHospitalPhone(hospital.getPhone());
                                                        voucher.setHospitalAcceptedDate(new Date());
                                                }
                                        } catch (Exception e) {
                                                System.err.println("Error fetching hospital info: " + e.getMessage());
                                        }

                                        voucher.setNotes((notes != null ? notes : "") + " [Validated by Hospital ID: "
                                                        + hospitalId + "]");
                                        voucher = rewardRedemptionRepository.save(voucher);
                                }
                        }

                        return ResponseEntity.ok(
                                        ResponseUtils.buildSuccessResponse(
                                                        HttpStatus.OK,
                                                        "Voucher status updated successfully",
                                                        voucher));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                                                        e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                                                        e.getMessage()));
                }
        }

        /**
         * Get validation history for a hospital - only shows vouchers validated by this
         * specific hospital
         * Security: Each hospital can only access their own validation history
         */
        @GetMapping("/hospital/{hospitalId}/validations")
        @PreAuthorize("hasRole('HOSPITAL')")
        public ResponseEntity<?> getHospitalValidationHistory(@PathVariable String hospitalId) {
                try {
                        // Validate hospital ID
                        if (hospitalId == null || hospitalId.trim().isEmpty()) {
                                return ResponseEntity.badRequest().body(
                                                ResponseUtils.buildErrorResponse(
                                                                HttpStatus.BAD_REQUEST,
                                                                "Hospital ID is required"));
                        }

                        // Additional security: In a production environment, you might want to verify
                        // that the hospitalId matches the authenticated user's ID
                        // This prevents hospitals from accessing other hospitals' validation history

                        // Get medical service vouchers validated by this specific hospital (including cancelled ones)
                        List<RewardRedemption> validations = rewardRedemptionRepository
                                        .findByValidatedByHospitalIdAndRewardTypeOrderByUpdatedAtDesc(
                                                        hospitalId, "MEDICAL_SERVICE");

                        // Enrich with donor information and hospital details
                        List<Map<String, Object>> enrichedValidations = validations.stream().map(voucher -> {
                                User donor = userRepository.findById(voucher.getDonorId()).orElse(null);

                                Map<String, Object> enriched = new HashMap<>();
                                enriched.put("id", voucher.getId());
                                enriched.put("voucherCode", voucher.getVoucherCode());
                                enriched.put("rewardTitle", voucher.getRewardTitle());
                                enriched.put("rewardType", voucher.getRewardType());
                                enriched.put("status", voucher.getStatus());
                                enriched.put("donorId", voucher.getDonorId());
                                enriched.put("donorName", donor != null ? donor.getName() : "Unknown");
                                enriched.put("donorEmail", donor != null ? donor.getEmail() : "Unknown");
                                enriched.put("redeemedDate", voucher.getRedeemedDate());
                                enriched.put("updatedAt", voucher.getUpdatedAt());
                                enriched.put("notes", voucher.getNotes());

                                // Add hospital validation details
                                enriched.put("validatedByHospitalId", voucher.getValidatedByHospitalId());
                                enriched.put("hospitalName", voucher.getHospitalName());
                                enriched.put("hospitalAcceptedDate", voucher.getHospitalAcceptedDate());

                                // Add validation date (when the hospital processed it)
                                enriched.put("validationDate", voucher.getUpdatedAt());

                                return enriched;
                        }).toList();

                        return ResponseEntity.ok(
                                        ResponseUtils.buildSuccessResponse(
                                                        HttpStatus.OK,
                                                        "Hospital validation history retrieved successfully",
                                                        enrichedValidations));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                        ResponseUtils.buildErrorResponse(
                                                        HttpStatus.INTERNAL_SERVER_ERROR,
                                                        "Error retrieving validation history: " + e.getMessage()));
        }
    }

    /**
     * Get all reward redemptions grouped by hospitals and blood banks (for admin)
     */
    @GetMapping("/admin/redemptions")
    public ResponseEntity<?> getAllRedemptionsGrouped() {
        try {
            List<RewardRedemption> redemptions = rewardPointsManagementService.getAllRedemptions();
            
            // Group redemptions by which hospital/blood bank validated them
            Map<String, Object> groupedData = new HashMap<>();
            
            // Get all hospitals and blood banks from their respective repositories
            List<Hospital> hospitals = hospitalRepository.findAll();
            List<RedSource.entities.BloodBankUser> bloodBanks = bloodBankUserRepository.findAll();
            
            // Group redemptions by hospital validation
            Map<String, Object> hospitalGroups = new HashMap<>();
            for (Hospital hospital : hospitals) {
                List<RewardRedemption> hospitalRedemptions = redemptions.stream()
                    .filter(r -> r.getValidatedByHospitalId() != null && 
                               r.getValidatedByHospitalId().equals(hospital.getId()))
                    .toList();
                
                // Always include hospital, even if no redemptions
                Map<String, Object> hospitalData = new HashMap<>();
                hospitalData.put("id", hospital.getId());
                hospitalData.put("name", hospital.getHospitalName());
                hospitalData.put("totalRedemptions", hospitalRedemptions.size());
                hospitalData.put("redemptions", hospitalRedemptions.stream().map(redemption -> {
                    Map<String, Object> redemptionMap = new HashMap<>();
                    redemptionMap.put("id", redemption.getId());
                    redemptionMap.put("rewardTitle", redemption.getRewardTitle());
                    redemptionMap.put("status", redemption.getStatus());
                    redemptionMap.put("redeemedDate", redemption.getRedeemedDate());
                    return redemptionMap;
                }).toList());
                
                hospitalGroups.put(hospital.getId(), hospitalData);
            }
            
            // Group redemptions by blood bank validation
            Map<String, Object> bloodBankGroups = new HashMap<>();
            for (RedSource.entities.BloodBankUser bloodBank : bloodBanks) {
                List<RewardRedemption> bloodBankRedemptions = redemptions.stream()
                    .filter(r -> r.getValidatedByBloodBankId() != null && 
                               r.getValidatedByBloodBankId().equals(bloodBank.getId()))
                    .toList();
                
                // Always include blood bank, even if no redemptions
                Map<String, Object> bloodBankData = new HashMap<>();
                bloodBankData.put("id", bloodBank.getId());
                bloodBankData.put("name", bloodBank.getBloodBankName());
                bloodBankData.put("totalRedemptions", bloodBankRedemptions.size());
                bloodBankData.put("redemptions", bloodBankRedemptions.stream().map(redemption -> {
                    Map<String, Object> redemptionMap = new HashMap<>();
                    redemptionMap.put("id", redemption.getId());
                    redemptionMap.put("rewardTitle", redemption.getRewardTitle());
                    redemptionMap.put("status", redemption.getStatus());
                    redemptionMap.put("redeemedDate", redemption.getRedeemedDate());
                    return redemptionMap;
                }).toList());
                
                bloodBankGroups.put(bloodBank.getId(), bloodBankData);
            }
            
            groupedData.put("hospitals", hospitalGroups);
            groupedData.put("bloodBanks", bloodBankGroups);
            
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Grouped redemptions retrieved successfully",
                            groupedData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            e.getMessage()));
        }
    }
} 
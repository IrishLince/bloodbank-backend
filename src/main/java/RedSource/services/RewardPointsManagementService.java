package RedSource.services;

import RedSource.entities.BloodBankUser;
import RedSource.entities.Hospital;
import RedSource.entities.PointTransaction;
import RedSource.entities.RewardRedemption;
import RedSource.entities.User;
import RedSource.repositories.PointTransactionRepository;
import RedSource.repositories.RewardRedemptionRepository;
import RedSource.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RewardPointsManagementService {

    private static final Logger log = LoggerFactory.getLogger(RewardPointsManagementService.class);

    private final UserRepository userRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final RewardRedemptionRepository rewardRedemptionRepository;
    private final BloodInventoryService bloodInventoryService;
    private final BloodBankService bloodBankService;
    private final HospitalService hospitalService;

    // Points configuration
    private static final int POINTS_PER_DONATION = 100;
    private static final int POINTS_PER_REFERRAL = 50;
    private static final int BRONZE_MILESTONE_BONUS = 100;
    private static final int SILVER_MILESTONE_BONUS = 200;
    private static final int GOLD_MILESTONE_BONUS = 500;

    // Tier thresholds
    private static final int CERTIFIED_THRESHOLD = 1;
    private static final int BRONZE_THRESHOLD = 5;
    private static final int SILVER_THRESHOLD = 10;
    private static final int GOLD_THRESHOLD = 25;

    /**
     * Award points to a donor for a completed donation
     */
    @Transactional
    public User awardDonationPoints(String donorId, String donationId) {
        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        // Award donation points
        int currentPoints = donor.getRewardPoints() != null ? donor.getRewardPoints() : 0;
        int newPoints = currentPoints + POINTS_PER_DONATION;
        donor.setRewardPoints(newPoints);

        // Increment total donations
        int totalDonations = donor.getTotalDonations() != null ? donor.getTotalDonations() : 0;
        totalDonations++;
        donor.setTotalDonations(totalDonations);

        // Update tier
        String newTier = calculateTier(totalDonations);
        donor.setDonorTier(newTier);

        // Check for milestone bonus
        int milestoneBonus = getMilestoneBonus(totalDonations);
        if (milestoneBonus > 0) {
            newPoints += milestoneBonus;
            donor.setRewardPoints(newPoints);

            // Record milestone bonus transaction
            PointTransaction milestoneTransaction = PointTransaction.builder()
                    .donorId(donorId)
                    .points(milestoneBonus)
                    .transactionType("MILESTONE")
                    .description(newTier + " Donor Milestone Bonus")
                    .relatedEntityId(donationId)
                    .balanceAfter(newPoints)
                    .createdAt(new Date())
                    .build();
            pointTransactionRepository.save(milestoneTransaction);
        }

        donor.setUpdatedAt(new Date());
        donor = userRepository.save(donor);

        // Record donation points transaction
        PointTransaction transaction = PointTransaction.builder()
                .donorId(donorId)
                .points(POINTS_PER_DONATION)
                .transactionType("DONATION")
                .description("Blood Donation")
                .relatedEntityId(donationId)
                .balanceAfter(milestoneBonus > 0 ? newPoints : currentPoints + POINTS_PER_DONATION)
                .createdAt(new Date())
                .build();
        pointTransactionRepository.save(transaction);

        return donor;
    }

    /**
     * Award referral bonus points
     */
    @Transactional
    public User awardReferralPoints(String donorId, String referredUserId) {
        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        int currentPoints = donor.getRewardPoints() != null ? donor.getRewardPoints() : 0;
        int newPoints = currentPoints + POINTS_PER_REFERRAL;
        donor.setRewardPoints(newPoints);
        donor.setUpdatedAt(new Date());
        donor = userRepository.save(donor);

        // Record referral transaction
        PointTransaction transaction = PointTransaction.builder()
                .donorId(donorId)
                .points(POINTS_PER_REFERRAL)
                .transactionType("REFERRAL")
                .description("Referral Bonus")
                .relatedEntityId(referredUserId)
                .balanceAfter(newPoints)
                .createdAt(new Date())
                .build();
        pointTransactionRepository.save(transaction);

        return donor;
    }

    /**
     * Redeem a reward
     */
    @Transactional
    public RewardRedemption redeemReward(String donorId, String rewardTitle, String rewardType, int pointsCost, String tier, String redeemableAt) {
        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        int currentPoints = donor.getRewardPoints() != null ? donor.getRewardPoints() : 0;
        if (currentPoints < pointsCost) {
            throw new RuntimeException("Insufficient points");
        }

        // Deduct points
        int newPoints = currentPoints - pointsCost;
        donor.setRewardPoints(newPoints);
        donor.setUpdatedAt(new Date());
        userRepository.save(donor);

        // Calculate expiry date (1 month from redemption)
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, 1);
        Date expiryDate = calendar.getTime();

        // Create redemption record
        RewardRedemption redemption = RewardRedemption.builder()
                .donorId(donorId)
                .rewardTitle(rewardTitle)
                .rewardType(rewardType)
                .tier(tier)
                .redeemableAt(redeemableAt)
                .pointsCost(pointsCost)
                .status("PENDING")
                .redeemedDate(new Date())
                .expiryDate(expiryDate)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        // Generate voucher code for applicable rewards
        if ("BLOOD_BAG_VOUCHER".equals(rewardType) || "GIFT_CARD".equals(rewardType)
                || "MEDICAL_SERVICE".equals(rewardType)) {
            redemption.setVoucherCode(generateVoucherCode());
        }

        redemption = rewardRedemptionRepository.save(redemption);

        // Record redemption transaction
        PointTransaction transaction = PointTransaction.builder()
                .donorId(donorId)
                .points(-pointsCost)
                .transactionType("REDEMPTION")
                .description(rewardTitle)
                .relatedEntityId(redemption.getId())
                .balanceAfter(newPoints)
                .createdAt(new Date())
                .build();
        pointTransactionRepository.save(transaction);

        return redemption;
    }

    /**
     * Get donor's point transaction history
     */
    public List<PointTransaction> getPointHistory(String donorId) {
        return pointTransactionRepository.findByDonorIdOrderByCreatedAtDesc(donorId);
    }

    /**
     * Get donor's redemption history with blood bank information
     */
    public List<Map<String, Object>> getRedemptionHistory(String donorId) {
        List<RewardRedemption> redemptions = rewardRedemptionRepository.findByDonorIdOrderByRedeemedDateDesc(donorId);
        return enrichRedemptionsWithBloodBankInfo(redemptions);
    }

    /**
     * Enrich redemptions with blood bank information
     */
    private List<Map<String, Object>> enrichRedemptionsWithBloodBankInfo(List<RewardRedemption> redemptions) {
        return redemptions.stream().map(redemption -> {
            Map<String, Object> enrichedRedemption = new HashMap<>();

            // Copy all original fields
            enrichedRedemption.put("id", redemption.getId());
            enrichedRedemption.put("donorId", redemption.getDonorId());
            enrichedRedemption.put("rewardTitle", redemption.getRewardTitle());
            enrichedRedemption.put("rewardType", redemption.getRewardType());
            enrichedRedemption.put("pointsCost", redemption.getPointsCost());
            enrichedRedemption.put("status", redemption.getStatus());
            enrichedRedemption.put("redeemedDate", redemption.getRedeemedDate());
            enrichedRedemption.put("deliveredDate", redemption.getDeliveredDate());
            enrichedRedemption.put("voucherCode", redemption.getVoucherCode());
            enrichedRedemption.put("expiryDate", redemption.getExpiryDate());
            enrichedRedemption.put("notes", redemption.getNotes());
            enrichedRedemption.put("validatedByBloodBankId", redemption.getValidatedByBloodBankId());
            enrichedRedemption.put("createdAt", redemption.getCreatedAt());
            enrichedRedemption.put("updatedAt", redemption.getUpdatedAt());

            // Add blood bank information if available
            if (redemption.getValidatedByBloodBankId() != null) {
                try {
                    BloodBankUser bloodBank = bloodBankService.getById(redemption.getValidatedByBloodBankId());
                    if (bloodBank != null) {
                        Map<String, Object> bloodBankInfo = new HashMap<>();
                        bloodBankInfo.put("id", bloodBank.getId());
                        bloodBankInfo.put("name", bloodBank.getBloodBankName());
                        bloodBankInfo.put("address", bloodBank.getAddress());
                        bloodBankInfo.put("phone", bloodBank.getPhone());
                        bloodBankInfo.put("email", bloodBank.getEmail());
                        bloodBankInfo.put("operatingHours", bloodBank.getOperatingHours());
                        enrichedRedemption.put("bloodBankInfo", bloodBankInfo);
                    }
                } catch (Exception e) {
                    // Log error but don't fail the entire request
                    System.err.println("Error fetching blood bank info for ID " + redemption.getValidatedByBloodBankId()
                            + ": " + e.getMessage());
                }
            }

            // Add hospital information if available (for medical service vouchers)
            if (redemption.getValidatedByHospitalId() != null) {
                try {
                    Hospital hospital = hospitalService.getEntityById(redemption.getValidatedByHospitalId());
                    if (hospital != null) {
                        Map<String, Object> hospitalInfo = new HashMap<>();
                        hospitalInfo.put("id", hospital.getId());
                        hospitalInfo.put("name", hospital.getHospitalName());
                        hospitalInfo.put("address", hospital.getAddress());
                        hospitalInfo.put("phone", hospital.getPhone());
                        hospitalInfo.put("email", hospital.getEmail());
                        hospitalInfo.put("operatingHours", hospital.getOperatingHours());
                        hospitalInfo.put("acceptedDate", redemption.getHospitalAcceptedDate());
                        enrichedRedemption.put("hospitalInfo", hospitalInfo);
                    }
                } catch (Exception e) {
                    // Log error but don't fail the entire request
                    System.err.println("Error fetching hospital info for ID " + redemption.getValidatedByHospitalId()
                            + ": " + e.getMessage());
                }
            } else if (redemption.getHospitalName() != null) {
                // If we have hospital info stored directly in the redemption (for backward
                // compatibility)
                Map<String, Object> hospitalInfo = new HashMap<>();
                hospitalInfo.put("name", redemption.getHospitalName());
                hospitalInfo.put("address", redemption.getHospitalAddress());
                hospitalInfo.put("phone", redemption.getHospitalPhone());
                hospitalInfo.put("acceptedDate", redemption.getHospitalAcceptedDate());
                enrichedRedemption.put("hospitalInfo", hospitalInfo);
            }

            return enrichedRedemption;
        }).toList();
    }

    /**
     * Update redemption status
     */
    @Transactional
    public RewardRedemption updateRedemptionStatus(String redemptionId, String status) {
        RewardRedemption redemption = rewardRedemptionRepository.findById(redemptionId)
                .orElseThrow(() -> new RuntimeException("Redemption not found"));

        redemption.setStatus(status);
        redemption.setUpdatedAt(new Date());

        if ("COMPLETED".equals(status)) {
            redemption.setDeliveredDate(new Date());
        }

        return rewardRedemptionRepository.save(redemption);
    }

    /**
     * Calculate donor tier based on total donations
     */
    private String calculateTier(int totalDonations) {
        if (totalDonations >= GOLD_THRESHOLD) {
            return "GOLD";
        } else if (totalDonations >= SILVER_THRESHOLD) {
            return "SILVER";
        } else if (totalDonations >= BRONZE_THRESHOLD) {
            return "BRONZE";
        } else if (totalDonations >= CERTIFIED_THRESHOLD) {
            return "CERTIFIED";
        } else {
            return "NEW";
        }
    }

    /**
     * Get milestone bonus for specific donation count
     */
    private int getMilestoneBonus(int totalDonations) {
        if (totalDonations == GOLD_THRESHOLD) {
            return GOLD_MILESTONE_BONUS;
        } else if (totalDonations == SILVER_THRESHOLD) {
            return SILVER_MILESTONE_BONUS;
        } else if (totalDonations == BRONZE_THRESHOLD) {
            return BRONZE_MILESTONE_BONUS;
        }
        return 0;
    }

    /**
     * Generate unique voucher code
     */
    private String generateVoucherCode() {
        return "RDS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Get donor's current points balance
     */
    public int getPointsBalance(String donorId) {
        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found"));
        return donor.getRewardPoints() != null ? donor.getRewardPoints() : 0;
    }

    /**
     * Initialize reward points for existing donors
     */
    @Transactional
    public void initializeRewardPoints(String donorId) {
        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        if (donor.getRewardPoints() == null) {
            donor.setRewardPoints(0);
        }
        if (donor.getTotalDonations() == null) {
            donor.setTotalDonations(0);
        }
        if (donor.getDonorTier() == null) {
            donor.setDonorTier("NEW");
        }
        donor.setUpdatedAt(new Date());
        userRepository.save(donor);
    }

    // Blood Bank Voucher Management Methods

    /**
     * Get all redemptions (for admin)
     */
    public List<RewardRedemption> getAllRedemptions() {
        return rewardRedemptionRepository.findAll().stream()
                .sorted((a, b) -> b.getRedeemedDate().compareTo(a.getRedeemedDate()))
                .toList();
    }

    /**
     * Get all blood bag vouchers
     */
    public List<RewardRedemption> getAllBloodBagVouchers() {
        return rewardRedemptionRepository.findAll().stream()
                .filter(r -> "BLOOD_BAG_VOUCHER".equals(r.getRewardType()))
                .sorted((a, b) -> b.getRedeemedDate().compareTo(a.getRedeemedDate()))
                .toList();
    }

    /**
     * Get blood bag vouchers by status
     */
    public List<RewardRedemption> getBloodBagVouchersByStatus(String status) {
        return rewardRedemptionRepository.findAll().stream()
                .filter(r -> "BLOOD_BAG_VOUCHER".equals(r.getRewardType()) && status.equals(r.getStatus()))
                .sorted((a, b) -> b.getRedeemedDate().compareTo(a.getRedeemedDate()))
                .toList();
    }

    /**
     * Validate a voucher code
     */
    public RewardRedemption validateVoucherCode(String voucherCode) {
        return rewardRedemptionRepository.findAll().stream()
                .filter(r -> voucherCode.equals(r.getVoucherCode()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Validate a voucher code for blood bank redemption
     */
    public RewardRedemption validateVoucherCodeForBloodBank(String voucherCode) {
        RewardRedemption voucher = validateVoucherCode(voucherCode);
        
        if (voucher == null) {
            return null;
        }
        
        // Check if voucher is redeemable at blood bank
        String redeemableAt = voucher.getRedeemableAt();
        if (redeemableAt != null) {
            if ("HOSPITAL".equals(redeemableAt)) {
                throw new RuntimeException("This voucher is only redeemable at HOSPITALS.");
            }
            if (!"BLOODBANK".equals(redeemableAt) && !"BOTH".equals(redeemableAt)) {
                throw new RuntimeException("This voucher cannot be redeemed at blood banks");
            }
        }
        
        return voucher;
    }

    /**
     * Validate a voucher code for hospital redemption
     */
    public RewardRedemption validateVoucherCodeForHospital(String voucherCode) {
        RewardRedemption voucher = validateVoucherCode(voucherCode);
        
        if (voucher == null) {
            return null;
        }
        
        // Check if voucher is redeemable at hospital
        String redeemableAt = voucher.getRedeemableAt();
        if (redeemableAt != null) {
            if ("BLOODBANK".equals(redeemableAt)) {
                throw new RuntimeException("This voucher is only redeemable at BLOODBANKS.");
            }
            if (!"HOSPITAL".equals(redeemableAt) && !"BOTH".equals(redeemableAt)) {
                throw new RuntimeException("This voucher cannot be redeemed at hospitals");
            }
        }
        
        return voucher;
    }

    /**
     * Get vouchers validated by a specific blood bank (includes both accepted and rejected)
     */
    public List<RewardRedemption> getVouchersByBloodBank(String bloodBankId) {
        return rewardRedemptionRepository.findByValidatedByBloodBankId(bloodBankId);
    }

    /**
     * Get vouchers validated by a specific blood bank with a specific status
     */
    public List<RewardRedemption> getVouchersByBloodBankAndStatus(String bloodBankId, String status) {
        return rewardRedemptionRepository.findByValidatedByBloodBankIdAndStatus(bloodBankId, status);
    }

    /**
     * Accept a blood bag voucher
     */
    @Transactional
    public RewardRedemption acceptBloodBagVoucher(String voucherId, String bloodBankId) {
        RewardRedemption voucher = rewardRedemptionRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        if (!"BLOOD_BAG_VOUCHER".equals(voucher.getRewardType())) {
            throw new RuntimeException("Not a blood bag voucher");
        }

        if (!"PENDING".equals(voucher.getStatus())) {
            throw new RuntimeException("Voucher is not in pending status");
        }

        // Extract blood type from reward title (e.g., "Blood Bag Voucher - A+")
        String bloodType = extractBloodTypeFromTitle(voucher.getRewardTitle());

        // Validate inventory availability before accepting
        if (bloodType != null) {
            int availableUnits = bloodInventoryService.getAvailableUnits(bloodBankId, bloodType);
            if (availableUnits < 1) {
                throw new RuntimeException(
                        "Insufficient inventory: Blood type " + bloodType + " is not available (0 units in stock)");
            }
        }

        voucher.setStatus("PROCESSING");
        voucher.setUpdatedAt(new Date());
        voucher.setValidatedByBloodBankId(bloodBankId); // Track which blood bank accepted this

        // Set acceptance/delivered date (when blood bank accepted the voucher)
        voucher.setDeliveredDate(new Date());

        // Set expiry date (30 days from now)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 30);
        voucher.setExpiryDate(cal.getTime());

        // Deduct inventory when voucher is accepted (moved from completeBloodBagVoucher)
        if (bloodType != null) {
            try {
                bloodInventoryService.decreaseInventory(bloodBankId, bloodType, 1);
                log.info("Deducted 1 unit of {} from blood bank {} inventory for voucher {}", 
                    bloodType, bloodBankId, voucherId);
            } catch (Exception e) {
                log.error("Failed to deduct inventory for voucher {}: {}", voucherId, e.getMessage());
                throw new RuntimeException("Failed to process blood bag voucher: " + e.getMessage());
            }
        }

        return rewardRedemptionRepository.save(voucher);
    }

    /**
     * Accept a general voucher (non-blood bag vouchers like gift cards, medical services)
     */
    @Transactional
    public RewardRedemption acceptGeneralVoucher(String voucherId, String bloodBankId) {
        RewardRedemption voucher = rewardRedemptionRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        if ("BLOOD_BAG_VOUCHER".equals(voucher.getRewardType())) {
            throw new RuntimeException("Use acceptBloodBagVoucher for blood bag vouchers");
        }

        if (!"PENDING".equals(voucher.getStatus())) {
            throw new RuntimeException("Voucher is not in pending status");
        }

        voucher.setStatus("PROCESSING");
        voucher.setUpdatedAt(new Date());
        voucher.setValidatedByBloodBankId(bloodBankId);

        // Set acceptance/delivered date
        voucher.setDeliveredDate(new Date());

        log.info("General voucher accepted: {} by blood bank: {}", voucherId, bloodBankId);
        return rewardRedemptionRepository.save(voucher);
    }

    /**
     * Complete a voucher (any type redeemable at blood banks)
     */
    @Transactional
    public RewardRedemption completeBloodBagVoucher(String voucherId) {
        RewardRedemption voucher = rewardRedemptionRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        // Check if voucher is redeemable at blood banks
        String redeemableAt = voucher.getRedeemableAt();
        if (redeemableAt != null && !"BLOODBANK".equals(redeemableAt) && !"BOTH".equals(redeemableAt)) {
            throw new RuntimeException("This voucher is not redeemable at blood banks");
        }

        if (!"PROCESSING".equals(voucher.getStatus())) {
            throw new RuntimeException("Voucher must be in processing status to complete");
        }

        voucher.setStatus("COMPLETED");
        voucher.setDeliveredDate(new Date());
        voucher.setUpdatedAt(new Date());

        // Note: Inventory is already deducted when voucher is accepted (in acceptBloodBagVoucher)
        // This method only marks the voucher as completed

        return rewardRedemptionRepository.save(voucher);
    }

    /**
     * Extract blood type from reward title
     */
    private String extractBloodTypeFromTitle(String rewardTitle) {
        if (rewardTitle == null) {
            return null;
        }

        // Format: "Blood Bag Voucher - A+"
        String[] parts = rewardTitle.split(" - ");
        if (parts.length > 1) {
            return parts[1].trim();
        }

        return null;
    }

    /**
     * Reject a voucher (any type redeemable at blood bank)
     */
    @Transactional
    public RewardRedemption rejectBloodBagVoucher(String voucherId, String reason, String bloodBankId) {
        RewardRedemption voucher = rewardRedemptionRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        // Check if voucher is redeemable at blood bank
        String redeemableAt = voucher.getRedeemableAt();
        if (redeemableAt != null && "HOSPITAL".equals(redeemableAt)) {
            throw new RuntimeException("This voucher is only redeemable at hospitals, not blood banks");
        }

        if (!"PENDING".equals(voucher.getStatus())) {
            throw new RuntimeException("Can only reject pending vouchers");
        }

        voucher.setStatus("CANCELLED");
        voucher.setNotes(reason != null ? "Rejected: " + reason : "Rejected by blood bank");
        voucher.setValidatedByBloodBankId(bloodBankId);
        voucher.setUpdatedAt(new Date());

        // Refund points to donor
        User donor = userRepository.findById(voucher.getDonorId()).orElse(null);
        if (donor != null) {
            int currentPoints = donor.getRewardPoints() != null ? donor.getRewardPoints() : 0;
            donor.setRewardPoints(currentPoints + voucher.getPointsCost());
            donor.setUpdatedAt(new Date());
            userRepository.save(donor);

            // Record refund transaction
            PointTransaction refundTransaction = PointTransaction.builder()
                    .donorId(voucher.getDonorId())
                    .points(voucher.getPointsCost())
                    .transactionType("REFUND")
                    .description("Voucher rejected - Points refunded")
                    .relatedEntityId(voucherId)
                    .balanceAfter(currentPoints + voucher.getPointsCost())
                    .createdAt(new Date())
                    .build();
            pointTransactionRepository.save(refundTransaction);
        }

        return rewardRedemptionRepository.save(voucher);
    }

    /**
     * Cancel a medical service voucher and refund points
     */
    @Transactional
    public RewardRedemption cancelMedicalServiceVoucher(String voucherId, String reason) {
        RewardRedemption voucher = rewardRedemptionRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        if (!"MEDICAL_SERVICE".equals(voucher.getRewardType())) {
            throw new RuntimeException("Not a medical service voucher");
        }

        if ("CANCELLED".equals(voucher.getStatus()) || "COMPLETED".equals(voucher.getStatus())) {
            throw new RuntimeException("Cannot cancel a voucher that is already " + voucher.getStatus().toLowerCase());
        }

        voucher.setStatus("CANCELLED");
        voucher.setNotes(reason != null ? "Cancelled: " + reason : "Cancelled by hospital");
        voucher.setUpdatedAt(new Date());
        
        // Extract hospital ID from the reason if it contains it
        if (reason != null && reason.contains("[Hospital ID: ")) {
            String hospitalId = reason.substring(reason.indexOf("[Hospital ID: ") + 14, reason.indexOf("]"));
            voucher.setValidatedByHospitalId(hospitalId);
        }

        // Refund points to donor
        User donor = userRepository.findById(voucher.getDonorId()).orElse(null);
        if (donor != null) {
            int currentPoints = donor.getRewardPoints() != null ? donor.getRewardPoints() : 0;
            donor.setRewardPoints(currentPoints + voucher.getPointsCost());
            donor.setUpdatedAt(new Date());
            userRepository.save(donor);

            // Record refund transaction
            PointTransaction refundTransaction = PointTransaction.builder()
                    .donorId(voucher.getDonorId())
                    .points(voucher.getPointsCost())
                    .transactionType("REFUND")
                    .description("Medical service voucher cancelled - Points refunded: " + voucher.getRewardTitle())
                    .relatedEntityId(voucherId)
                    .balanceAfter(currentPoints + voucher.getPointsCost())
                    .createdAt(new Date())
                    .build();
            pointTransactionRepository.save(refundTransaction);
        }

        return rewardRedemptionRepository.save(voucher);
    }
}

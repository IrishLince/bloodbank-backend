package RedSource.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Date;

@Document(collection = "reward_redemptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RewardRedemption implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("donor_id")
    private String donorId;

    @Field("reward_title")
    private String rewardTitle;

    @Field("reward_type")
    private String rewardType; // BADGE, MEDICAL_SERVICE, GIFT_CARD, PRIORITY_BOOKING, BLOOD_BAG_VOUCHER

    @Field("tier")
    private String tier; // NEW, CERTIFIED, BRONZE, SILVER, GOLD

    @Field("redeemable_at")
    private String redeemableAt; // HOSPITAL, BLOODBANK, BOTH

    @Field("points_cost")
    private Integer pointsCost;

    @Field("status")
    private String status; // PENDING, PROCESSING, COMPLETED, CANCELLED

    @Field("redeemed_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date redeemedDate;

    @Field("delivered_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date deliveredDate;

    @Field("voucher_code")
    private String voucherCode; // For blood bag vouchers or gift cards

    @Field("expiry_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date expiryDate;

    @Field("notes")
    private String notes;

    @Field("validated_by_blood_bank_id")
    private String validatedByBloodBankId; // Blood bank that validated/accepted this voucher

    @Field("validated_by_hospital_id")
    private String validatedByHospitalId; // Hospital that validated/accepted this medical service voucher

    @Field("hospital_name")
    private String hospitalName; // Hospital name for quick access

    @Field("hospital_address")
    private String hospitalAddress; // Hospital address

    @Field("hospital_phone")
    private String hospitalPhone; // Hospital phone number

    @Field("hospital_accepted_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date hospitalAcceptedDate; // When the hospital accepted the voucher

    @Field("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date createdAt;

    @Field("updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date updatedAt;

    // Explicit getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDonorId() {
        return donorId;
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public String getRewardTitle() {
        return rewardTitle;
    }

    public void setRewardTitle(String rewardTitle) {
        this.rewardTitle = rewardTitle;
    }

    public String getRewardType() {
        return rewardType;
    }

    public void setRewardType(String rewardType) {
        this.rewardType = rewardType;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getRedeemableAt() {
        return redeemableAt;
    }

    public void setRedeemableAt(String redeemableAt) {
        this.redeemableAt = redeemableAt;
    }

    public Integer getPointsCost() {
        return pointsCost;
    }

    public void setPointsCost(Integer pointsCost) {
        this.pointsCost = pointsCost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getRedeemedDate() {
        return redeemedDate;
    }

    public void setRedeemedDate(Date redeemedDate) {
        this.redeemedDate = redeemedDate;
    }

    public Date getDeliveredDate() {
        return deliveredDate;
    }

    public void setDeliveredDate(Date deliveredDate) {
        this.deliveredDate = deliveredDate;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getValidatedByBloodBankId() {
        return validatedByBloodBankId;
    }

    public void setValidatedByBloodBankId(String validatedByBloodBankId) {
        this.validatedByBloodBankId = validatedByBloodBankId;
    }

    public String getValidatedByHospitalId() {
        return validatedByHospitalId;
    }

    public void setValidatedByHospitalId(String validatedByHospitalId) {
        this.validatedByHospitalId = validatedByHospitalId;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getHospitalAddress() {
        return hospitalAddress;
    }

    public void setHospitalAddress(String hospitalAddress) {
        this.hospitalAddress = hospitalAddress;
    }

    public String getHospitalPhone() {
        return hospitalPhone;
    }

    public void setHospitalPhone(String hospitalPhone) {
        this.hospitalPhone = hospitalPhone;
    }

    public Date getHospitalAcceptedDate() {
        return hospitalAcceptedDate;
    }

    public void setHospitalAcceptedDate(Date hospitalAcceptedDate) {
        this.hospitalAcceptedDate = hospitalAcceptedDate;
    }
}

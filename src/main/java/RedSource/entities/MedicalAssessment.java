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

@Document(collection = "medical_assessments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicalAssessment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("user_id")
    private String userId;

    @Field("blood_bank_id")
    private String bloodBankId;

    @Field("hemoglobin_level")
    private Double hemoglobinLevel;

    @Field("blood_pressure")
    private String bloodPressure;

    @Field("status")
    private String status;

    @Field("notes")
    private String notes;

    @Field("assessment_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date assessmentDate;

    @Field("allergies")
    private String allergies;

    @Field("pre_existing_conditions")
    private String preExistingConditions;

    @Field("medications")
    private String medications;

    @Field("recent_surgeries")
    private String recentSurgeries;

    @Field("family_medical_history")
    private String familyMedicalHistory;

    @Field("kg_weight")
    private Double kgWeight;

    @Field("lbs_weight")
    private Double lbsWeight;

    @Field("cm_height")
    private Double cmHeight;

    @Field("ft_in_height")
    private String ftInHeight;

    @Field("doctor_id")
    private String doctorId;

    @Field("appointment_id")
    private String appointmentId;

    @Field("created_at")
    private Date createdAt;

    @Field("updated_at")
    private Date updatedAt;

    // Explicit getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBloodBankId() {
        return bloodBankId;
    }

    public void setBloodBankId(String bloodBankId) {
        this.bloodBankId = bloodBankId;
    }

    public Double getHemoglobinLevel() {
        return hemoglobinLevel;
    }

    public void setHemoglobinLevel(Double hemoglobinLevel) {
        this.hemoglobinLevel = hemoglobinLevel;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(Date assessmentDate) {
        this.assessmentDate = assessmentDate;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getPreExistingConditions() {
        return preExistingConditions;
    }

    public void setPreExistingConditions(String preExistingConditions) {
        this.preExistingConditions = preExistingConditions;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getRecentSurgeries() {
        return recentSurgeries;
    }

    public void setRecentSurgeries(String recentSurgeries) {
        this.recentSurgeries = recentSurgeries;
    }

    public String getFamilyMedicalHistory() {
        return familyMedicalHistory;
    }

    public void setFamilyMedicalHistory(String familyMedicalHistory) {
        this.familyMedicalHistory = familyMedicalHistory;
    }

    public Double getKgWeight() {
        return kgWeight;
    }

    public void setKgWeight(Double kgWeight) {
        this.kgWeight = kgWeight;
    }

    public Double getLbsWeight() {
        return lbsWeight;
    }

    public void setLbsWeight(Double lbsWeight) {
        this.lbsWeight = lbsWeight;
    }

    public Double getCmHeight() {
        return cmHeight;
    }

    public void setCmHeight(Double cmHeight) {
        this.cmHeight = cmHeight;
    }

    public String getFtInHeight() {
        return ftInHeight;
    }

    public void setFtInHeight(String ftInHeight) {
        this.ftInHeight = ftInHeight;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
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

    public boolean isMetric() {
        return kgWeight != null && cmHeight != null;
    }

    public boolean isImperial() {
        return lbsWeight != null && ftInHeight != null;
    }
}

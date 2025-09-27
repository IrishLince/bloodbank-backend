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
import java.util.List;

@Document(collection = "hospital_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HospitalRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("hospital_id")
    private String hospitalId;

    @Field("blood_bank_id")
    private String bloodBankId;

    @Field("blood_bank_name")
    private String bloodBankName;

    @Field("blood_bank_address")
    private String bloodBankAddress;

    @Field("blood_bank_phone")
    private String bloodBankPhone;

    @Field("blood_bank_email")
    private String bloodBankEmail;

    @Field("blood_items")
    @com.fasterxml.jackson.annotation.JsonProperty("bloodItems")
    private List<BloodInventoryItem> bloodItems;

    @Field("status")
    private String status;

    @Field("request_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date requestDate;

    @Field("date_needed")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date dateNeeded;

    @Field("hospital_name")
    private String hospitalName;

    @Field("hospital_address")
    private String hospitalAddress;

    @Field("contact_information")
    private String contactInformation;

    @Field("notes")
    private String notes;

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

    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getBloodBankId() {
        return bloodBankId;
    }

    public void setBloodBankId(String bloodBankId) {
        this.bloodBankId = bloodBankId;
    }

    public String getBloodBankName() {
        return bloodBankName;
    }

    public void setBloodBankName(String bloodBankName) {
        this.bloodBankName = bloodBankName;
    }

    public String getBloodBankAddress() {
        return bloodBankAddress;
    }

    public void setBloodBankAddress(String bloodBankAddress) {
        this.bloodBankAddress = bloodBankAddress;
    }

    public String getBloodBankPhone() {
        return bloodBankPhone;
    }

    public void setBloodBankPhone(String bloodBankPhone) {
        this.bloodBankPhone = bloodBankPhone;
    }

    public String getBloodBankEmail() {
        return bloodBankEmail;
    }

    public void setBloodBankEmail(String bloodBankEmail) {
        this.bloodBankEmail = bloodBankEmail;
    }

    public List<BloodInventoryItem> getBloodItems() {
        return bloodItems;
    }

    public void setBloodItems(List<BloodInventoryItem> bloodItems) {
        this.bloodItems = bloodItems;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public Date getDateNeeded() {
        return dateNeeded;
    }

    public void setDateNeeded(Date dateNeeded) {
        this.dateNeeded = dateNeeded;
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

    public String getContactInformation() {
        return contactInformation;
    }

    public void setContactInformation(String contactInformation) {
        this.contactInformation = contactInformation;
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
}

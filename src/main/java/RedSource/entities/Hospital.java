package RedSource.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

@Document(collection = "users_hospital")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Hospital implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("hospital_name")
    private String hospitalName;

    @Field("email")
    private String email;

    @Field("username")
    private String username;

    @JsonIgnore
    @Field("password")
    private String password;

    @Field("phone")
    private String phone;

    @Field("address")
    private String address;

    @Field("hospital_id")
    private String hospitalId;

    @Field("license_number")
    private String licenseNumber;

    @Field("profile_photo_url")
    private String profilePhotoUrl;

    @Field("created_at")
    private Date createdAt;

    @Field("updated_at")
    private Date updatedAt;

    // Additional fields for donation center functionality
    @Field("coordinates")
    private Coordinates coordinates;

    @Field("operating_hours")
    private String operatingHours;

    @Field("blood_types_available")
    private List<String> bloodTypesAvailable;

    @Field("is_donation_center")
    private Boolean isDonationCenter;

    @Field("urgent_need")
    private Boolean urgentNeed;

    // Nested class for coordinates
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Coordinates {
        private Double lat;
        private Double lng;
    }

    // Explicit getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
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

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public String getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(String operatingHours) {
        this.operatingHours = operatingHours;
    }

    public List<String> getBloodTypesAvailable() {
        return bloodTypesAvailable;
    }

    public void setBloodTypesAvailable(List<String> bloodTypesAvailable) {
        this.bloodTypesAvailable = bloodTypesAvailable;
    }

    public Boolean getIsDonationCenter() {
        return isDonationCenter;
    }

    public void setIsDonationCenter(Boolean isDonationCenter) {
        this.isDonationCenter = isDonationCenter;
    }

    public Boolean getUrgentNeed() {
        return urgentNeed;
    }

    public void setUrgentNeed(Boolean urgentNeed) {
        this.urgentNeed = urgentNeed;
    }
}
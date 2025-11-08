package RedSource.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import RedSource.entities.enums.UserRoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

import java.io.Serializable;
import java.util.Date;

@Document(collection = "users")
@CompoundIndexes({
    @CompoundIndex(name = "email_role_idx", def = "{'email': 1, 'role': 1}"),
    @CompoundIndex(name = "contact_role_idx", def = "{'contact_information': 1, 'role': 1}"),
    @CompoundIndex(name = "status_created_idx", def = "{'account_status': 1, 'created_at': -1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User implements Serializable {

    private static final long serialVersionUID = 5364496601245150928L;

    @Id
    private String id;

    @Field("name")
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Field("email")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Indexed(unique = true)
    private String email;

    @Field("username")
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
    @Indexed(unique = true)
    private String username;

    @JsonIgnore
    @Field("password")
    private String password;

    @Field("role")
    @NotNull(message = "User role is required")
    private UserRoleType role;

    @Field("contact_information")
    @Pattern(regexp = "^(63\\d{10}|\\d{10})$", message = "Phone number must be in valid Philippine format")
    @Indexed
    private String contactInformation;

    @Field("date_of_birth")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dateOfBirth;

    @Field("blood_type")
    @Pattern(regexp = "^(A[+-]|B[+-]|AB[+-]|O[+-])$", message = "Blood type must be valid (A+, A-, B+, B-, AB+, AB-, O+, O-)")
    private String bloodType;

    @Field("address")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
    
    @Field("age")
    @Min(value = 16, message = "Age must be at least 16")
    @Max(value = 120, message = "Age must not exceed 120")
    private Integer age;
    
    @Field("sex")
    @Pattern(regexp = "^(Male|Female|Other)$", message = "Sex must be Male, Female, or Other")
    private String sex;

    @Field("profile_photo_url")
    private String profilePhotoUrl;

    // Reward Points System Fields
    @Field("reward_points")
    @Builder.Default
    @Min(value = 0, message = "Reward points cannot be negative")
    @Indexed
    private Integer rewardPoints = 0;

    @Field("total_donations")
    @Builder.Default
    @Min(value = 0, message = "Total donations cannot be negative")
    @Indexed
    private Integer totalDonations = 0;

    @Field("donor_tier")
    @Pattern(regexp = "^(NEW|CERTIFIED|BRONZE|SILVER|GOLD)$", message = "Donor tier must be NEW, CERTIFIED, BRONZE, SILVER, or GOLD")
    @Indexed
    private String donorTier; // NEW, CERTIFIED, BRONZE, SILVER, GOLD

    @Field("account_status")
    @Builder.Default
    @Pattern(regexp = "^(ACTIVE|ARCHIVED|SUSPENDED)$", message = "Account status must be ACTIVE, ARCHIVED, or SUSPENDED")
    @Indexed
    private String accountStatus = "ACTIVE"; // ACTIVE, ARCHIVED, SUSPENDED

    @Field("created_at")
    @Builder.Default
    @Indexed
    private Date createdAt = new Date();

    @Field("updated_at")
    @Indexed
    private Date updatedAt;

    // Explicit getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public UserRoleType getRole() {
        return role;
    }

    public void setRole(UserRoleType role) {
        this.role = role;
    }

    public String getContactInformation() {
        return contactInformation;
    }

    public void setContactInformation(String contactInformation) {
        this.contactInformation = contactInformation;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }
    
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    
    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
    
    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
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

    public Integer getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(Integer rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public Integer getTotalDonations() {
        return totalDonations;
    }

    public void setTotalDonations(Integer totalDonations) {
        this.totalDonations = totalDonations;
    }

    public String getDonorTier() {
        return donorTier;
    }

    public void setDonorTier(String donorTier) {
        this.donorTier = donorTier;
    }

    public String getAccountStatus() {
        // Return "ACTIVE" as default if accountStatus is null (for existing users)
        return accountStatus != null ? accountStatus : "ACTIVE";
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
}

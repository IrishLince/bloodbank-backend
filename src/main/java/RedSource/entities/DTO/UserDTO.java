package RedSource.entities.DTO;

import RedSource.entities.User;
import RedSource.entities.enums.UserRoleType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String email;
    private String username;
    private UserRoleType role;
    private String contactInformation;
    private String bloodType;
    private String address;
    private Integer age;
    private String sex;
    private String profilePhotoUrl;
    private Date dateOfBirth;
    private Date createdAt;
    private Date updatedAt;
    
    // Reward Points System Fields
    private Integer rewardPoints;
    private Integer totalDonations;
    private String donorTier;
    
    @JsonProperty("accountStatus")
    private String accountStatus;

    /**
     * Constructs a UserDTO from a User entity.
     *
     * @param user the User entity
     */
    public UserDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.role = user.getRole();
        this.contactInformation = user.getContactInformation();
        this.bloodType = user.getBloodType();
        this.address = user.getAddress();
        this.age = user.getAge();
        this.sex = user.getSex();
        this.profilePhotoUrl = user.getProfilePhotoUrl();
        this.dateOfBirth = user.getDateOfBirth();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.rewardPoints = user.getRewardPoints();
        this.totalDonations = user.getTotalDonations();
        this.accountStatus = user.getAccountStatus();
        this.donorTier = user.getDonorTier();
        
        // Debug log
        System.out.println("UserDTO created - Account Status: " + this.accountStatus);
    }

    public User toEntity() {
        return User.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .username(this.username)
                .role(this.role)
                .contactInformation(this.contactInformation)
                .bloodType(this.bloodType)
                .address(this.address)
                .age(this.age)
                .sex(this.sex)
                .profilePhotoUrl(this.profilePhotoUrl)
                .dateOfBirth(this.dateOfBirth)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .rewardPoints(this.rewardPoints)
                .totalDonations(this.totalDonations)
                .donorTier(this.donorTier)
                .build();
    }
}

package RedSource.entities.RO;

import RedSource.entities.User;
import RedSource.entities.enums.UserRoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRO {

    private String id;

    @NotBlank(message = "Name is mandatory.")
    private String name;

    @NotBlank(message = "Email is mandatory.")
    @Email(message = "Invalid email format.")
    private String email;

    @NotBlank(message = "Password is mandatory.")
    private String password;

    @NotBlank(message = "Contact information is mandatory.")
    private String contactInformation;

    @NotBlank(message = "Role is mandatory.")
    private String role;

    @NotBlank(message = "Blood Type is mandatory.")
    private String bloodType;

    private Date dateOfBirth;

    /**
     * Converts this RO to a User entity.
     *
     * @param user the User entity to populate
     * @return the populated User entity
     */
    public User toEntity(User user) {
        if (user == null) {
            user = new User();
        }

        user.setName(this.name);
        user.setEmail(this.email);
        user.setContactInformation(this.contactInformation);
        user.setRole(UserRoleType.valueOf(this.role));
        user.setBloodType(this.bloodType);
        user.setDateOfBirth(this.dateOfBirth);

        if (this.password != null && !this.password.isBlank()) {
            user.setPassword(this.password);
        }

        return user;
    }
}
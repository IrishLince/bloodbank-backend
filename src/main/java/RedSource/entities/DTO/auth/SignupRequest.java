package RedSource.entities.DTO.auth;

import RedSource.entities.enums.UserRoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class SignupRequest {
    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    private UserRoleType role;
    
    private String contactInformation;
    
    private String bloodType;
    
    private String address;
    
    private Integer age;
    
    private String sex;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date birthDate;
}
package RedSource.entities.DTO;

import RedSource.entities.Hospital;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HospitalDTO {
    
    private String id;
    private String hospitalName;
    private String email;
    private String username;
    private String phone;
    private String address;
    private String hospitalId;
    private String licenseNumber;
    private String profilePhotoUrl;
    private Date createdAt;
    private Date updatedAt;

    // Constructor from Hospital entity
    public HospitalDTO(Hospital hospital) {
        this.id = hospital.getId();
        this.hospitalName = hospital.getHospitalName();
        this.email = hospital.getEmail();
        this.username = hospital.getUsername();
        this.phone = hospital.getPhone();
        this.address = hospital.getAddress();
        this.hospitalId = hospital.getHospitalId();
        this.licenseNumber = hospital.getLicenseNumber();
        this.profilePhotoUrl = hospital.getProfilePhotoUrl();
        this.createdAt = hospital.getCreatedAt();
        this.updatedAt = hospital.getUpdatedAt();
    }

    // Convert to Hospital entity
    public Hospital toEntity() {
        return Hospital.builder()
                .id(this.id)
                .hospitalName(this.hospitalName)
                .email(this.email)
                .username(this.username)
                .phone(this.phone)
                .address(this.address)
                .hospitalId(this.hospitalId)
                .licenseNumber(this.licenseNumber)
                .profilePhotoUrl(this.profilePhotoUrl)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}

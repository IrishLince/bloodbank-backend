package RedSource.security;

import RedSource.entities.User;
import RedSource.entities.UserAdmin;
import RedSource.entities.Hospital;
import RedSource.entities.BloodBankUser;
import RedSource.repositories.UserRepository;
import RedSource.repositories.UserAdminRepository;
import RedSource.repositories.HospitalRepository;
import RedSource.repositories.BloodBankUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    UserAdminRepository userAdminRepository;
    
    @Autowired
    HospitalRepository hospitalRepository;
    
    @Autowired
    BloodBankUserRepository bloodBankUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // First, try to find in admin users collection
        Optional<UserAdmin> adminOpt = userAdminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            // Convert UserAdmin to User for authentication
            UserAdmin admin = adminOpt.get();
            User adminAsUser = User.builder()
                    .id(admin.getId())
                    .name(admin.getName())
                    .email(admin.getEmail())
                    .username(admin.getUsername())
                    .password(admin.getPassword())
                    .role(RedSource.entities.enums.UserRoleType.ADMIN)
                    .build();
            return UserDetailsImpl.build(adminAsUser);
        }
        
        // Then, try to find in regular users collection
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return UserDetailsImpl.build(userOpt.get());
        }
        
        // Then, try to find in hospitals collection
        Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(email);
        if (hospitalOpt.isPresent()) {
            // Convert Hospital to User for authentication
            Hospital hospital = hospitalOpt.get();
            User hospitalAsUser = User.builder()
                    .id(hospital.getId())
                    .name(hospital.getHospitalName())
                    .email(hospital.getEmail())
                    .username(hospital.getUsername())
                    .password(hospital.getPassword())
                    .role(RedSource.entities.enums.UserRoleType.HOSPITAL)
                    .contactInformation(hospital.getPhone())
                    .profilePhotoUrl(hospital.getProfilePhotoUrl())
                    .createdAt(hospital.getCreatedAt())
                    .updatedAt(hospital.getUpdatedAt())
                    .build();
            return UserDetailsImpl.build(hospitalAsUser);
        }
        
        // Finally, try to find in blood bank users collection
        Optional<BloodBankUser> bloodBankUserOpt = bloodBankUserRepository.findByEmail(email);
        if (bloodBankUserOpt.isPresent()) {
            // Convert BloodBankUser to User for authentication
            BloodBankUser bloodBankUser = bloodBankUserOpt.get();
            User bloodBankAsUser = User.builder()
                    .id(bloodBankUser.getId())
                    .name(bloodBankUser.getBloodBankName())
                    .email(bloodBankUser.getEmail())
                    .username(bloodBankUser.getUsername())
                    .password(bloodBankUser.getPassword())
                    .role(RedSource.entities.enums.UserRoleType.BLOODBANK)
                    .contactInformation(bloodBankUser.getPhone())
                    .profilePhotoUrl(bloodBankUser.getProfilePhotoUrl())
                    .createdAt(bloodBankUser.getCreatedAt())
                    .updatedAt(bloodBankUser.getUpdatedAt())
                    .build();
            return UserDetailsImpl.build(bloodBankAsUser);
        }
        
        throw new UsernameNotFoundException("User Not Found with email: " + email);
    }
}
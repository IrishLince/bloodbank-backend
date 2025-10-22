package RedSource.services;

import RedSource.entities.Hospital;
import RedSource.entities.DTO.HospitalDTO;
import RedSource.repositories.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final PasswordEncoder passwordEncoder;

    public List<HospitalDTO> getAll() {
        return hospitalRepository.findAll()
                .stream()
                .map(HospitalDTO::new)
                .collect(Collectors.toList());
    }

    public HospitalDTO getById(String id) {
        Optional<Hospital> hospital = hospitalRepository.findById(id);
        return hospital.map(HospitalDTO::new).orElse(null);
    }

    public Hospital getEntityById(String id) {
        Optional<Hospital> hospital = hospitalRepository.findById(id);
        return hospital.orElse(null);
    }

    public HospitalDTO getByEmail(String email) {
        Optional<Hospital> hospital = hospitalRepository.findByEmail(email);
        return hospital.map(HospitalDTO::new).orElse(null);
    }

    public HospitalDTO getByUsername(String username) {
        Optional<Hospital> hospital = hospitalRepository.findByUsername(username);
        return hospital.map(HospitalDTO::new).orElse(null);
    }

    public HospitalDTO save(HospitalDTO hospitalDTO) {
        Hospital hospital = hospitalDTO.toEntity();

        // Set timestamps
        Date now = new Date();
        if (hospital.getId() == null) {
            hospital.setCreatedAt(now);
        }
        hospital.setUpdatedAt(now);

        // Encode password if provided
        if (hospital.getPassword() != null && !hospital.getPassword().isEmpty()) {
            hospital.setPassword(passwordEncoder.encode(hospital.getPassword()));
        }

        Hospital savedHospital = hospitalRepository.save(hospital);
        return new HospitalDTO(savedHospital);
    }

    public HospitalDTO update(String id, HospitalDTO hospitalDTO) {
        Optional<Hospital> existingHospitalOpt = hospitalRepository.findById(id);
        if (!existingHospitalOpt.isPresent()) {
            throw new RuntimeException("Hospital not found with id: " + id);
        }

        Hospital existingHospital = existingHospitalOpt.get();
        Hospital updatedHospital = hospitalDTO.toEntity();

        // Preserve certain fields
        updatedHospital.setId(id);
        updatedHospital.setCreatedAt(existingHospital.getCreatedAt());
        updatedHospital.setUpdatedAt(new Date());

        // Keep existing password if not provided in update
        if (updatedHospital.getPassword() == null || updatedHospital.getPassword().isEmpty()) {
            updatedHospital.setPassword(existingHospital.getPassword());
        } else {
            updatedHospital.setPassword(passwordEncoder.encode(updatedHospital.getPassword()));
        }

        Hospital savedHospital = hospitalRepository.save(updatedHospital);
        return new HospitalDTO(savedHospital);
    }

    public void delete(String id) {
        if (!hospitalRepository.existsById(id)) {
            throw new RuntimeException("Hospital not found with id: " + id);
        }
        hospitalRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return hospitalRepository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return hospitalRepository.existsByUsername(username);
    }

    public boolean existsByHospitalId(String hospitalId) {
        return hospitalRepository.existsByHospitalId(hospitalId);
    }

    public HospitalDTO updatePassword(String id, String newPassword) {
        Optional<Hospital> hospitalOpt = hospitalRepository.findById(id);
        if (!hospitalOpt.isPresent()) {
            throw new RuntimeException("Hospital not found with id: " + id);
        }

        Hospital hospital = hospitalOpt.get();
        hospital.setPassword(passwordEncoder.encode(newPassword));
        hospital.setUpdatedAt(new Date());

        Hospital savedHospital = hospitalRepository.save(hospital);
        return new HospitalDTO(savedHospital);
    }

    public HospitalDTO updateProfilePhoto(String id, String photoUrl) {
        Optional<Hospital> hospitalOpt = hospitalRepository.findById(id);
        if (!hospitalOpt.isPresent()) {
            throw new RuntimeException("Hospital not found with id: " + id);
        }

        Hospital hospital = hospitalOpt.get();
        hospital.setProfilePhotoUrl(photoUrl);
        hospital.setUpdatedAt(new Date());

        Hospital savedHospital = hospitalRepository.save(hospital);
        return new HospitalDTO(savedHospital);
    }
}

package RedSource.repositories;

import RedSource.entities.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital, String> {
    
    Optional<Hospital> findByEmail(String email);
    
    Optional<Hospital> findByUsername(String username);
    
    Optional<Hospital> findByHospitalId(String hospitalId);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByHospitalId(String hospitalId);
}

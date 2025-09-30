package RedSource.repositories;

import RedSource.entities.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital, String> {
    
    Optional<Hospital> findByEmail(String email);
    
    Optional<Hospital> findByUsername(String username);
    
    Optional<Hospital> findByHospitalId(String hospitalId);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByHospitalId(String hospitalId);
    
    // Find hospitals that are donation centers
    List<Hospital> findByIsDonationCenterTrue();
    
    // Find by hospital name (case insensitive)
    @Query("{'hospital_name': {$regex: ?0, $options: 'i'}, 'is_donation_center': true}")
    List<Hospital> findByHospitalNameContainingIgnoreCaseAndIsDonationCenterTrue(String name);
    
    // Find by address/location (case insensitive)
    @Query("{'address': {$regex: ?0, $options: 'i'}, 'is_donation_center': true}")
    List<Hospital> findByAddressContainingIgnoreCaseAndIsDonationCenterTrue(String address);
    
    // Find hospitals within coordinate bounds (for location-based search)
    @Query("{'coordinates.lat': {$gte: ?0, $lte: ?1}, 'coordinates.lng': {$gte: ?2, $lte: ?3}, 'is_donation_center': true}")
    List<Hospital> findByCoordinatesWithinBoundsAndIsDonationCenterTrue(Double minLat, Double maxLat, Double minLng, Double maxLng);
    
    // Find urgent hospitals
    List<Hospital> findByUrgentNeedTrueAndIsDonationCenterTrue();
}

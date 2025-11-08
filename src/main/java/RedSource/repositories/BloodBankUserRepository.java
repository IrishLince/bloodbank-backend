package RedSource.repositories;

import RedSource.entities.BloodBankUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BloodBankUserRepository extends MongoRepository<BloodBankUser, String> {

    Optional<BloodBankUser> findByEmail(String email);

    Optional<BloodBankUser> findByUsername(String username);

    Optional<BloodBankUser> findByBloodBankId(String bloodBankId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByBloodBankId(String bloodBankId);

    // Search methods for donation centers
    @Query("{'name': {$regex: ?0, $options: 'i'}}")
    List<BloodBankUser> findByBloodBankNameContainingIgnoreCase(String name);

    @Query("{'address': {$regex: ?0, $options: 'i'}}")
    List<BloodBankUser> findByAddressContainingIgnoreCase(String address);

    BloodBankUser findByBloodBankName(String bloodBankName);
}

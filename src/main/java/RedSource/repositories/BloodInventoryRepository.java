package RedSource.repositories;

import RedSource.entities.BloodInventory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodInventoryRepository extends MongoRepository<BloodInventory, String> {
    @Query("{ 'blood_bank_id': ?0 }")
    List<BloodInventory> findByBloodBankId(String bloodBankId);
}
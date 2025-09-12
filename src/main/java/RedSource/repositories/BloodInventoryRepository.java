package RedSource.repositories;

import RedSource.entities.BloodInventory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BloodInventoryRepository extends MongoRepository<BloodInventory, String> {
} 
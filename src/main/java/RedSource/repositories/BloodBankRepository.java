package RedSource.repositories;

import RedSource.entities.BloodBank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BloodBankRepository extends MongoRepository<BloodBank, String> {
    BloodBank findByName(String name);
} 
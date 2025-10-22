package RedSource.repositories;

import RedSource.entities.BloodBankUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BloodBankRepository extends MongoRepository<BloodBankUser, String> {
    BloodBankUser findByBloodBankName(String bloodBankName);
}

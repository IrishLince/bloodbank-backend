package RedSource.repositories;

import RedSource.entities.VitalSigns;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VitalSignsRepository extends MongoRepository<VitalSigns, String> {
} 
package RedSource.repositories;

import RedSource.entities.Fulfillment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FulfillmentRepository extends MongoRepository<Fulfillment, String> {
} 
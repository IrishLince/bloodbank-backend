package RedSource.repositories;

import RedSource.entities.Delivery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRepository extends MongoRepository<Delivery, String> {
    List<Delivery> findByRequestId(String requestId);
    List<Delivery> findAllByStatus(String status);
    List<Delivery> findByRequestIdIn(List<String> requestIds);
}

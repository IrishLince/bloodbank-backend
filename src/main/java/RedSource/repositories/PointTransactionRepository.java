package RedSource.repositories;

import RedSource.entities.PointTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointTransactionRepository extends MongoRepository<PointTransaction, String> {
    List<PointTransaction> findByDonorIdOrderByCreatedAtDesc(String donorId);
    List<PointTransaction> findByDonorIdAndTransactionType(String donorId, String transactionType);
}

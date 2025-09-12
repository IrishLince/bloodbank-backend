package RedSource.repositories;

import RedSource.entities.DonorHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonorHistoryRepository extends MongoRepository<DonorHistory, String> {
    List<DonorHistory> findByDonorId(String donorId);
    List<DonorHistory> findByBloodBankId(String bloodBankId);
} 
package RedSource.repositories;

import RedSource.entities.RewardPoints;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RewardPointsRepository extends MongoRepository<RewardPoints, String> {
    Optional<RewardPoints> findByDonorId(String donorId);
} 
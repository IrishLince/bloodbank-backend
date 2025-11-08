package RedSource.repositories;

import RedSource.entities.Reward;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardRepository extends MongoRepository<Reward, String> {
    List<Reward> findByIsActive(Boolean isActive);
    List<Reward> findByRedeemableAt(String redeemableAt);
    List<Reward> findByRewardType(String rewardType);
    List<Reward> findByIsActiveAndRedeemableAt(Boolean isActive, String redeemableAt);
}

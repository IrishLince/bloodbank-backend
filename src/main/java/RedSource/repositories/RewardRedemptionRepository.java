package RedSource.repositories;

import RedSource.entities.RewardRedemption;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardRedemptionRepository extends MongoRepository<RewardRedemption, String> {
    List<RewardRedemption> findByDonorIdOrderByRedeemedDateDesc(String donorId);

    List<RewardRedemption> findByDonorIdAndStatus(String donorId, String status);

    List<RewardRedemption> findByDonorIdAndRewardType(String donorId, String rewardType);

    @Query("{ 'validated_by_blood_bank_id': ?0 }")
    List<RewardRedemption> findByValidatedByBloodBankId(String bloodBankId);

    @Query("{ 'validated_by_blood_bank_id': ?0, 'status': ?1 }")
    List<RewardRedemption> findByValidatedByBloodBankIdAndStatus(String bloodBankId, String status);

    @Query("{ 'reward_type': ?0, 'status': { $ne: ?1 } }")
    List<RewardRedemption> findByRewardTypeAndStatusNotOrderByUpdatedAtDesc(String rewardType, String excludedStatus);

    @Query("{ 'validated_by_hospital_id': ?0, 'reward_type': ?1 }")
    List<RewardRedemption> findByValidatedByHospitalIdAndRewardTypeOrderByUpdatedAtDesc(String hospitalId,
            String rewardType);

    @Query("{ 'validated_by_hospital_id': ?0, 'reward_type': ?1, 'status': { $ne: ?2 } }")
    List<RewardRedemption> findByValidatedByHospitalIdAndRewardTypeAndStatusNotOrderByUpdatedAtDesc(String hospitalId,
            String rewardType, String excludedStatus);
}

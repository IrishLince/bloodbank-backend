package RedSource.services;

import RedSource.entities.Reward;
import RedSource.entities.utils.MessageUtils;
import RedSource.exceptions.ServiceException;
import RedSource.repositories.RewardRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RewardService {

    private static final Logger log = LoggerFactory.getLogger(RewardService.class);
    public static final String REWARD = "Reward";
    public static final String REWARDS = "Rewards";

    private final RewardRepository rewardRepository;

    public List<Reward> getAll() {
        try {
            List<Reward> rewards = rewardRepository.findAll();
            log.info(MessageUtils.retrieveSuccess(REWARDS));
            return rewards;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(REWARDS);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public List<Reward> getAllActive() {
        try {
            List<Reward> rewards = rewardRepository.findByIsActive(true);
            log.info(MessageUtils.retrieveSuccess("Active " + REWARDS));
            return rewards;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError("Active " + REWARDS);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public List<Reward> getByRedeemableAt(String redeemableAt) {
        try {
            List<Reward> rewards = rewardRepository.findByIsActiveAndRedeemableAt(true, redeemableAt);
            log.info("Retrieved rewards redeemable at: {}", redeemableAt);
            return rewards;
        } catch (Exception e) {
            String errorMessage = "Error retrieving rewards for " + redeemableAt;
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public Reward getById(String id) {
        try {
            if (Objects.isNull(id)) {
                return null;
            }
            return rewardRepository.findById(id).orElse(null);
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(REWARD);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public Reward save(Reward reward) {
        try {
            reward.setCreatedAt(new Date());
            reward.setUpdatedAt(new Date());
            reward.setIsActive(reward.getIsActive() != null ? reward.getIsActive() : true);
            
            Reward savedReward = rewardRepository.save(reward);
            log.info("Reward saved successfully: {}", savedReward.getId());
            return savedReward;
        } catch (Exception e) {
            String errorMessage = MessageUtils.saveError(REWARD);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public Reward update(String id, Reward reward) {
        try {
            Reward existingReward = rewardRepository.findById(id)
                    .orElseThrow(() -> new ServiceException("Reward not found with id: " + id));

            existingReward.setTitle(reward.getTitle());
            existingReward.setDescription(reward.getDescription());
            existingReward.setPointsCost(reward.getPointsCost());
            existingReward.setRewardType(reward.getRewardType());
            existingReward.setTier(reward.getTier());
            existingReward.setAutoUnlock(reward.getAutoUnlock());
            existingReward.setUnlockCondition(reward.getUnlockCondition());
            existingReward.setImage(reward.getImage());
            existingReward.setIsActive(reward.getIsActive());
            existingReward.setRedeemableAt(reward.getRedeemableAt());
            existingReward.setUpdatedAt(new Date());

            Reward updatedReward = rewardRepository.save(existingReward);
            log.info("Reward updated successfully: {}", updatedReward.getId());
            return updatedReward;
        } catch (Exception e) {
            String errorMessage = MessageUtils.updateError(REWARD);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public void delete(String id) {
        try {
            if (!rewardRepository.existsById(id)) {
                throw new ServiceException("Reward not found with id: " + id);
            }
            rewardRepository.deleteById(id);
            log.info("Reward deleted successfully: {}", id);
        } catch (Exception e) {
            String errorMessage = MessageUtils.deleteError(REWARD);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public Reward toggleActive(String id) {
        try {
            Reward reward = rewardRepository.findById(id)
                    .orElseThrow(() -> new ServiceException("Reward not found with id: " + id));
            
            reward.setIsActive(!reward.getIsActive());
            reward.setUpdatedAt(new Date());
            
            Reward updatedReward = rewardRepository.save(reward);
            log.info("Reward active status toggled to: {}", updatedReward.getIsActive());
            return updatedReward;
        } catch (Exception e) {
            String errorMessage = "Error toggling reward active status";
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }
}

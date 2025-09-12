package RedSource.services;

import RedSource.entities.RewardPoints;
import RedSource.entities.utils.MessageUtils;
import RedSource.exceptions.ServiceException;
import RedSource.repositories.RewardPointsRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RewardPointsService {

    private static final Logger log = LoggerFactory.getLogger(RewardPointsService.class);
    private final RewardPointsRepository rewardPointsRepository;

    public List<RewardPoints> getAll() {
        try {
            List<RewardPoints> rewardPointsList = rewardPointsRepository.findAll();
            log.info(MessageUtils.retrieveSuccess("Reward Points"));
            return rewardPointsList;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError("Reward Points");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public RewardPoints getById(String id) {
        try {
            if (Objects.isNull(id)) {
                return null;
            }
            return rewardPointsRepository.findById(id).orElse(null);
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError("Reward Points");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public RewardPoints save(RewardPoints rewardPoints) {
        try {
            rewardPoints.setCreatedAt(new Date());
            rewardPoints.setUpdatedAt(new Date());
            rewardPoints.setLastUpdated(new Date());
            RewardPoints savedRewardPoints = rewardPointsRepository.save(rewardPoints);
            log.info(MessageUtils.saveSuccess("Reward Points"));
            return savedRewardPoints;
        } catch (Exception e) {
            String errorMessage = MessageUtils.saveError("Reward Points");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public RewardPoints update(String id, RewardPoints rewardPoints) {
        try {
            RewardPoints existingRewardPoints = getById(id);
            if (existingRewardPoints == null) {
                throw new ServiceException("Reward Points not found");
            }
            rewardPoints.setId(id);
            rewardPoints.setCreatedAt(existingRewardPoints.getCreatedAt());
            rewardPoints.setUpdatedAt(new Date());
            rewardPoints.setLastUpdated(new Date());
            RewardPoints updatedRewardPoints = rewardPointsRepository.save(rewardPoints);
            log.info(MessageUtils.updateSuccess("Reward Points"));
            return updatedRewardPoints;
        } catch (Exception e) {
            String errorMessage = MessageUtils.updateError("Reward Points");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    public void delete(String id) {
        try {
            RewardPoints rewardPoints = getById(id);
            if (rewardPoints == null) {
                throw new ServiceException("Reward Points not found");
            }
            rewardPointsRepository.deleteById(id);
            log.info(MessageUtils.deleteSuccess("Reward Points"));
        } catch (Exception e) {
            String errorMessage = MessageUtils.deleteError("Reward Points");
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }
} 
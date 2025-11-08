package RedSource.controllers;

import RedSource.entities.Reward;
import RedSource.services.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RewardController {

    private static final Logger logger = LoggerFactory.getLogger(RewardController.class);

    private final RewardService rewardService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRewards() {
        logger.debug("GET /api/rewards - Retrieving all rewards");
        try {
            List<Reward> rewards = rewardService.getAll();
            logger.info("GET /api/rewards - Successfully retrieved {} rewards", rewards.size());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Rewards retrieved successfully",
                    "data", rewards
            ));
        } catch (Exception e) {
            logger.error("GET /api/rewards - Error retrieving rewards: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage(),
                            "data", null
                    ));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getAllActiveRewards() {
        logger.debug("GET /api/rewards/active - Retrieving all active rewards");
        try {
            List<Reward> rewards = rewardService.getAllActive();
            logger.info("GET /api/rewards/active - Successfully retrieved {} active rewards", rewards.size());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Active rewards retrieved successfully",
                    "data", rewards
            ));
        } catch (Exception e) {
            logger.error("GET /api/rewards/active - Error retrieving active rewards: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage(),
                            "data", null
                    ));
        }
    }

    @GetMapping("/redeemable-at/{redeemableAt}")
    public ResponseEntity<Map<String, Object>> getRewardsByRedeemableAt(@PathVariable String redeemableAt) {
        logger.debug("GET /api/rewards/redeemable-at/{} - Retrieving rewards by redeemable location", redeemableAt);
        try {
            List<Reward> rewards = rewardService.getByRedeemableAt(redeemableAt);
            logger.info("GET /api/rewards/redeemable-at/{} - Successfully retrieved {} rewards", redeemableAt, rewards.size());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Rewards retrieved successfully",
                    "data", rewards
            ));
        } catch (Exception e) {
            logger.error("GET /api/rewards/redeemable-at/{} - Error retrieving rewards: {}", redeemableAt, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage(),
                            "data", null
                    ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRewardById(@PathVariable String id) {
        logger.debug("GET /api/rewards/{} - Retrieving reward by ID", id);
        try {
            Reward reward = rewardService.getById(id);
            if (reward == null) {
                logger.warn("GET /api/rewards/{} - Reward not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "Reward not found",
                                "data", null
                        ));
            }
            logger.info("GET /api/rewards/{} - Successfully retrieved reward: {}", id, reward.getTitle());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Reward retrieved successfully",
                    "data", reward
            ));
        } catch (Exception e) {
            logger.error("GET /api/rewards/{} - Error retrieving reward: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage(),
                            "data", null
                    ));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createReward(@RequestBody Reward reward) {
        logger.debug("POST /api/rewards - Creating new reward: {}", reward != null ? reward.getTitle() : "null");
        try {
            Reward createdReward = rewardService.save(reward);
            logger.info("POST /api/rewards - Successfully created reward: {} (ID: {})", createdReward.getTitle(), createdReward.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "Reward created successfully",
                            "data", createdReward
                    ));
        } catch (Exception e) {
            logger.error("POST /api/rewards - Error creating reward: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage(),
                            "data", null
                    ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateReward(@PathVariable String id, @RequestBody Reward reward) {
        logger.debug("PUT /api/rewards/{} - Updating reward", id);
        try {
            Reward updatedReward = rewardService.update(id, reward);
            if (updatedReward == null) {
                logger.warn("PUT /api/rewards/{} - Reward not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "Reward not found",
                                "data", null
                        ));
            }
            logger.info("PUT /api/rewards/{} - Successfully updated reward: {}", id, updatedReward.getTitle());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Reward updated successfully",
                    "data", updatedReward
            ));
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            HttpStatus status = (message.contains("not found") || message.contains("does not exist")) 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            if (status == HttpStatus.NOT_FOUND) {
                logger.warn("PUT /api/rewards/{} - Update failed (not found): {}", id, e.getMessage());
            } else {
                logger.error("PUT /api/rewards/{} - Error updating reward: {}", id, e.getMessage(), e);
            }
            return ResponseEntity.status(status)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage(),
                            "data", null
                    ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReward(@PathVariable String id) {
        logger.debug("DELETE /api/rewards/{} - Deleting reward", id);
        try {
            rewardService.delete(id);
            logger.info("DELETE /api/rewards/{} - Successfully deleted reward", id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Reward deleted successfully"
            ));
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            HttpStatus status = (message.contains("not found") || message.contains("does not exist")) 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            if (status == HttpStatus.NOT_FOUND) {
                logger.warn("DELETE /api/rewards/{} - Delete failed (not found): {}", id, e.getMessage());
            } else {
                logger.error("DELETE /api/rewards/{} - Error deleting reward: {}", id, e.getMessage(), e);
            }
            return ResponseEntity.status(status)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Map<String, Object>> toggleRewardActive(@PathVariable String id) {
        logger.debug("PATCH /api/rewards/{}/toggle-active - Toggling reward active status", id);
        try {
            Reward updatedReward = rewardService.toggleActive(id);
            if (updatedReward == null) {
                logger.warn("PATCH /api/rewards/{}/toggle-active - Reward not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "Reward not found",
                                "data", null
                        ));
            }
            logger.info("PATCH /api/rewards/{}/toggle-active - Successfully toggled reward active status: {} (Active: {})", 
                    id, updatedReward.getTitle(), updatedReward.getIsActive());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Reward active status updated successfully",
                    "data", updatedReward
            ));
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            HttpStatus status = (message.contains("not found") || message.contains("does not exist")) 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            if (status == HttpStatus.NOT_FOUND) {
                logger.warn("PATCH /api/rewards/{}/toggle-active - Toggle failed (not found): {}", id, e.getMessage());
            } else {
                logger.error("PATCH /api/rewards/{}/toggle-active - Error toggling reward active status: {}", id, e.getMessage(), e);
            }
            return ResponseEntity.status(status)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage(),
                            "data", null
                    ));
        }
    }
}

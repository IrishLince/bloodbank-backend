package RedSource.controllers;

import RedSource.entities.Reward;
import RedSource.services.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RewardController {

    private final RewardService rewardService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRewards() {
        try {
            List<Reward> rewards = rewardService.getAll();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Rewards retrieved successfully",
                    "data", rewards
            ));
        } catch (Exception e) {
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
        try {
            List<Reward> rewards = rewardService.getAllActive();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Active rewards retrieved successfully",
                    "data", rewards
            ));
        } catch (Exception e) {
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
        try {
            List<Reward> rewards = rewardService.getByRedeemableAt(redeemableAt);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Rewards retrieved successfully",
                    "data", rewards
            ));
        } catch (Exception e) {
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
        try {
            Reward reward = rewardService.getById(id);
            if (reward == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", "Reward not found",
                                "data", null
                        ));
            }
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Reward retrieved successfully",
                    "data", reward
            ));
        } catch (Exception e) {
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
        try {
            Reward createdReward = rewardService.save(reward);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "Reward created successfully",
                            "data", createdReward
                    ));
        } catch (Exception e) {
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
        try {
            Reward updatedReward = rewardService.update(id, reward);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Reward updated successfully",
                    "data", updatedReward
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage(),
                            "data", null
                    ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReward(@PathVariable String id) {
        try {
            rewardService.delete(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Reward deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Map<String, Object>> toggleRewardActive(@PathVariable String id) {
        try {
            Reward updatedReward = rewardService.toggleActive(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Reward active status updated successfully",
                    "data", updatedReward
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage(),
                            "data", null
                    ));
        }
    }
}

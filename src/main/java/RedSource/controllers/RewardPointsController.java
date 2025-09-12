package RedSource.controllers;

import RedSource.entities.RewardPoints;
import RedSource.entities.utils.MessageUtils;
import RedSource.entities.utils.ResponseUtils;
import RedSource.services.RewardPointsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reward-points")
@RequiredArgsConstructor
public class RewardPointsController {

    public static final String REWARD_POINTS = "Reward Points";
    public static final String REWARD_POINTS_PLURAL = "Reward Points";

    private final RewardPointsService rewardPointsService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.retrieveSuccess(REWARD_POINTS_PLURAL),
                        rewardPointsService.getAll()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        RewardPoints rewardPoints = rewardPointsService.getById(id);
        if (rewardPoints == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.NOT_FOUND,
                            "Reward points not found"
                    )
            );
        }
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.retrieveSuccess(REWARD_POINTS),
                        rewardPoints
                )
        );
    }

    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody RewardPoints rewardPoints, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            MessageUtils.validationErrors(bindingResult)
                    )
            );
        }
        RewardPoints savedRewardPoints = rewardPointsService.save(rewardPoints);
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.saveSuccess(REWARD_POINTS),
                        savedRewardPoints
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody RewardPoints rewardPoints, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            MessageUtils.validationErrors(bindingResult)
                    )
            );
        }
        try {
            RewardPoints updatedRewardPoints = rewardPointsService.update(id, rewardPoints);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.updateSuccess(REWARD_POINTS),
                            updatedRewardPoints
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.NOT_FOUND,
                            e.getMessage()
                    )
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            rewardPointsService.delete(id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.deleteSuccess(REWARD_POINTS)
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.NOT_FOUND,
                            e.getMessage()
                    )
            );
        }
    }
} 
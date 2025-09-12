package RedSource.controllers;

import RedSource.entities.DonorHistory;
import RedSource.entities.utils.MessageUtils;
import RedSource.entities.utils.ResponseUtils;
import RedSource.services.DonorHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('DONOR') or hasRole('ADMIN')")
@RestController
@RequestMapping("/api/donor-history")
@RequiredArgsConstructor
public class DonorHistoryController {

    public static final String DONOR_HISTORY = "Donor History";
    public static final String DONOR_HISTORIES = "Donor Histories";

    private final DonorHistoryService donorHistoryService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.retrieveSuccess(DONOR_HISTORIES),
                        donorHistoryService.getAll()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        DonorHistory donorHistory = donorHistoryService.getById(id);
        if (donorHistory == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.NOT_FOUND,
                            "Donor history not found"
                    )
            );
        }
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.retrieveSuccess(DONOR_HISTORY),
                        donorHistory
                )
        );
    }

    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody DonorHistory donorHistory, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            MessageUtils.validationErrors(bindingResult)
                    )
            );
        }
        DonorHistory savedDonorHistory = donorHistoryService.save(donorHistory);
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.saveSuccess(DONOR_HISTORY),
                        savedDonorHistory
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody DonorHistory donorHistory, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            MessageUtils.validationErrors(bindingResult)
                    )
            );
        }
        try {
            DonorHistory updatedDonorHistory = donorHistoryService.update(id, donorHistory);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.updateSuccess(DONOR_HISTORY),
                            updatedDonorHistory
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
            donorHistoryService.delete(id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.deleteSuccess(DONOR_HISTORY)
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
package RedSource.controllers;

import RedSource.entities.Fulfillment;
import RedSource.entities.utils.MessageUtils;
import RedSource.entities.utils.ResponseUtils;
import RedSource.services.FulfillmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fulfillments")
@RequiredArgsConstructor
public class FulfillmentController {

    public static final String FULFILLMENT = "Fulfillment";
    public static final String FULFILLMENTS = "Fulfillments";

    private final FulfillmentService fulfillmentService;

    // Get all fulfillments
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.retrieveSuccess(FULFILLMENTS),
                        fulfillmentService.getAll()
                )
        );
    }

    // Get fulfillment by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        Fulfillment fulfillment = fulfillmentService.getById(id);
        if (fulfillment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.NOT_FOUND,
                            "Fulfillment not found"
                    )
            );
        }
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.retrieveSuccess(FULFILLMENT),
                        fulfillment
                )
        );
    }

    // Save a new fulfillment
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody Fulfillment fulfillment, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            MessageUtils.validationErrors(bindingResult)
                    )
            );
        }
        Fulfillment savedFulfillment = fulfillmentService.save(fulfillment);
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.saveSuccess(FULFILLMENT),
                        savedFulfillment
                )
        );
    }

    // Update an existing fulfillment
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody Fulfillment fulfillment, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            MessageUtils.validationErrors(bindingResult)
                    )
            );
        }
        try {
            Fulfillment updatedFulfillment = fulfillmentService.update(id, fulfillment);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.updateSuccess(FULFILLMENT),
                            updatedFulfillment
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

    // Delete a fulfillment
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            fulfillmentService.delete(id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.deleteSuccess(FULFILLMENT)
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

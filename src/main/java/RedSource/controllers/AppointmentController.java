package RedSource.controllers;

import RedSource.entities.Appointment;
import RedSource.entities.utils.MessageUtils;
import RedSource.entities.utils.ResponseUtils;
import RedSource.services.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('DONOR') or hasRole('BLOODBANK') or hasRole('HOSPITAL') or hasRole('ADMIN')")
@RestController
@RequestMapping("/api/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);
    
    public static final String APPOINTMENTS = "Appointments";
    public static final String APPOINTMENT = "Appointment";

    private final AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.retrieveSuccess(APPOINTMENTS),
                        appointmentService.getAll()
                )
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.retrieveSuccess(APPOINTMENTS),
                        appointmentService.getByUserId(userId)
                )
        );
    }

    @GetMapping("/bloodbank/{bloodBankId}")
    public ResponseEntity<?> getByBloodBankId(@PathVariable String bloodBankId) {
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.retrieveSuccess(APPOINTMENTS),
                        appointmentService.getByBloodBankId(bloodBankId)
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.NOT_FOUND,
                            "Appointment not found"
                    )
            );
        }
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.retrieveSuccess(APPOINTMENT),
                        appointment
                )
        );
    }

    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody Appointment appointment, BindingResult bindingResult) {
        log.info("POST /api/appointment - Received appointment creation request");
        log.debug("Appointment data: donorId={}, userId={}, bloodBankId={}, appointmentDate={}", 
                appointment.getDonorId(), appointment.getUserId(), appointment.getBloodBankId(), appointment.getAppointmentDate());
        
        if (bindingResult.hasErrors()) {
            log.warn("Appointment validation errors: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            MessageUtils.validationErrors(bindingResult)
                    )
            );
        }
        
        try {
            Appointment savedAppointment = appointmentService.save(appointment);
            log.info("Appointment created successfully with ID: {}", savedAppointment.getId());
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.saveSuccess(APPOINTMENT),
                            savedAppointment
                    )
            );
        } catch (Exception e) {
            log.error("Error saving appointment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to save appointment: " + e.getMessage()
                    )
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody Appointment appointment, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            MessageUtils.validationErrors(bindingResult)
                    )
            );
        }
        try {
            Appointment updatedAppointment = appointmentService.update(id, appointment);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.updateSuccess(APPOINTMENT),
                            updatedAppointment
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

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestBody java.util.Map<String, String> statusUpdate) {
        try {
            String newStatus = statusUpdate.get("status");
            if (newStatus == null || newStatus.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                "Status is required"
                        )
                );
            }
            
            Appointment updatedAppointment = appointmentService.updateStatus(id, newStatus);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Appointment status updated successfully",
                            updatedAppointment
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
            appointmentService.delete(id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.deleteSuccess(APPOINTMENT)
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

    @PostMapping("/{id}/mark-missed")
    public ResponseEntity<?> markAsMissed(@PathVariable String id) {
        try {
            Appointment appointment = appointmentService.getById(id);
            if (appointment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Appointment not found"
                        )
                );
            }
            
            appointment.setStatus("Missed");
            Appointment updated = appointmentService.update(id, appointment);
            
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            "Appointment marked as missed",
                            updated
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            e.getMessage()
                    )
            );
        }
    }
}

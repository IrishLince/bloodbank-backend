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
        log.debug("GET /api/appointment - Retrieving all appointments");
        try {
            var appointments = appointmentService.getAll();
            log.info("GET /api/appointment - Successfully retrieved {} appointments", appointments.size());
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(APPOINTMENTS),
                            appointments
                    )
            );
        } catch (Exception e) {
            log.error("GET /api/appointment - Error retrieving appointments: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getByUserId(@PathVariable String userId) {
        log.debug("GET /api/appointment/user/{} - Retrieving appointments by user ID", userId);
        try {
            var appointments = appointmentService.getByUserId(userId);
            log.info("GET /api/appointment/user/{} - Successfully retrieved {} appointments", userId, appointments.size());
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(APPOINTMENTS),
                            appointments
                    )
            );
        } catch (Exception e) {
            log.error("GET /api/appointment/user/{} - Error retrieving appointments: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/bloodbank/{bloodBankId}")
    public ResponseEntity<?> getByBloodBankId(@PathVariable String bloodBankId) {
        log.debug("GET /api/appointment/bloodbank/{} - Retrieving appointments by blood bank ID", bloodBankId);
        try {
            var appointments = appointmentService.getByBloodBankId(bloodBankId);
            log.info("GET /api/appointment/bloodbank/{} - Successfully retrieved {} appointments", bloodBankId, appointments.size());
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(APPOINTMENTS),
                            appointments
                    )
            );
        } catch (Exception e) {
            log.error("GET /api/appointment/bloodbank/{} - Error retrieving appointments: {}", bloodBankId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        log.debug("GET /api/appointment/{} - Retrieving appointment by ID", id);
        try {
            Appointment appointment = appointmentService.getById(id);
            if (appointment == null) {
                log.warn("GET /api/appointment/{} - Appointment not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ResponseUtils.buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                "Appointment not found"
                        )
                );
            }
            log.info("GET /api/appointment/{} - Successfully retrieved appointment", id);
            return ResponseEntity.ok(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.OK,
                            MessageUtils.retrieveSuccess(APPOINTMENT),
                            appointment
                    )
            );
        } catch (Exception e) {
            log.error("GET /api/appointment/{} - Error retrieving appointment: {}", id, e.getMessage(), e);
            throw e;
        }
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
            // Validate 3-month waiting period for blood donations
            String userId = appointment.getUserId() != null ? appointment.getUserId() : appointment.getDonorId();
            if (userId != null && appointment.getAppointmentDate() != null) {
                boolean canBook = appointmentService.canBookAppointment(userId, appointment.getAppointmentDate());
                if (!canBook) {
                    java.util.Date nextEligibleDate = appointmentService.getNextEligibleDonationDate(userId);
                    String errorMessage = String.format(
                        "You must wait at least 3 months between blood donations for your health and safety. " +
                        "Your next eligible donation date is: %s",
                        nextEligibleDate != null ? new java.text.SimpleDateFormat("MMMM dd, yyyy").format(nextEligibleDate) : "unknown"
                    );
                    log.warn("Appointment booking denied for user {}: too soon after last appointment", userId);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                            ResponseUtils.buildErrorResponse(
                                    HttpStatus.BAD_REQUEST,
                                    errorMessage
                            )
                    );
                }
            }
            
            Appointment savedAppointment = appointmentService.save(appointment);
            log.info("Appointment created successfully with ID: {}", savedAppointment.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ResponseUtils.buildSuccessResponse(
                            HttpStatus.CREATED,
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
            if (updatedAppointment == null) {
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
                            MessageUtils.updateSuccess(APPOINTMENT),
                            updatedAppointment
                    )
            );
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            HttpStatus status = (message.contains("not found") || message.contains("does not exist")) 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(
                    ResponseUtils.buildErrorResponse(status, e.getMessage())
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
            if (updatedAppointment == null) {
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
                            "Appointment status updated successfully",
                            updatedAppointment
                    )
            );
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            HttpStatus status = (message.contains("not found") || message.contains("does not exist")) 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(
                    ResponseUtils.buildErrorResponse(status, e.getMessage())
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
            String message = e.getMessage().toLowerCase();
            HttpStatus status = (message.contains("not found") || message.contains("does not exist")) 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(
                    ResponseUtils.buildErrorResponse(status, e.getMessage())
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

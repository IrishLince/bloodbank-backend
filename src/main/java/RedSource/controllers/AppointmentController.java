package RedSource.controllers;

import RedSource.entities.Appointment;
import RedSource.entities.utils.MessageUtils;
import RedSource.entities.utils.ResponseUtils;
import RedSource.services.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ResponseUtils.buildErrorResponse(
                            HttpStatus.BAD_REQUEST,
                            MessageUtils.validationErrors(bindingResult)
                    )
            );
        }
        Appointment savedAppointment = appointmentService.save(appointment);
        return ResponseEntity.ok(
                ResponseUtils.buildSuccessResponse(
                        HttpStatus.OK,
                        MessageUtils.saveSuccess(APPOINTMENT),
                        savedAppointment
                )
        );
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
}

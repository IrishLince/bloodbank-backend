package RedSource.services;

import RedSource.entities.Appointment;
import RedSource.entities.utils.MessageUtils;
import RedSource.exceptions.ServiceException;
import RedSource.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);
    public static final String APPOINTMENTS = "Appointments";
    public static final String APPOINTMENT = "Appointment";

    private final AppointmentRepository appointmentRepository;

    // Retrieve all appointments without any filter
    public List<Appointment> getAll() {
        try {
            List<Appointment> appointments = appointmentRepository.findAll();
            log.info(MessageUtils.retrieveSuccess(APPOINTMENTS));
            return appointments;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(APPOINTMENTS);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    // Retrieve an appointment by ID
    public Appointment getById(String id) {
        try {
            if (Objects.isNull(id)) {
                return null;
            }
            return appointmentRepository.findById(id).orElse(null);
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(APPOINTMENT);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    // Retrieve appointments by user ID
    public List<Appointment> getByUserId(String userId) {
        try {
            if (Objects.isNull(userId)) {
                return List.of();
            }
            List<Appointment> appointments = appointmentRepository.findByUserId(userId);
            log.info(MessageUtils.retrieveSuccess(APPOINTMENTS));
            return appointments;
        } catch (Exception e) {
            String errorMessage = MessageUtils.retrieveError(APPOINTMENTS);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    // Save a new appointment
    public Appointment save(Appointment appointment) {
        try {
            Appointment savedAppointment = appointmentRepository.save(appointment);
            log.info(MessageUtils.saveSuccess(APPOINTMENT));
            return savedAppointment;
        } catch (Exception e) {
            String errorMessage = MessageUtils.saveError(APPOINTMENT);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    // Update an existing appointment
    public Appointment update(String id, Appointment appointment) {
        try {
            Appointment existingAppointment = getById(id);
            if (existingAppointment == null) {
                throw new ServiceException("Appointment not found", new RuntimeException("Appointment not found"));
            }
            appointment.setId(id);
            Appointment updatedAppointment = appointmentRepository.save(appointment);
            log.info(MessageUtils.updateSuccess(APPOINTMENT));
            return updatedAppointment;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = MessageUtils.updateError(APPOINTMENT);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    // Delete an appointment by ID
    public void delete(String id) {
        try {
            Appointment appointment = getById(id);
            if (appointment == null) {
                throw new ServiceException("Appointment not found", new RuntimeException("Appointment not found"));
            }
            appointmentRepository.delete(appointment);
            log.info(MessageUtils.deleteSuccess(APPOINTMENT));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = MessageUtils.deleteError(APPOINTMENT);
            log.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }
}

package RedSource.services;

import RedSource.entities.Appointment;
import RedSource.entities.utils.MessageUtils;
import RedSource.exceptions.ServiceException;
import RedSource.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);
    public static final String APPOINTMENTS = "Appointments";
    public static final String APPOINTMENT = "Appointment";

    private final AppointmentRepository appointmentRepository;
    private final RewardPointsManagementService rewardPointsManagementService;

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

    // Retrieve appointments by blood bank ID
    public List<Appointment> getByBloodBankId(String bloodBankId) {
        try {
            if (Objects.isNull(bloodBankId)) {
                return List.of();
            }
            List<Appointment> appointments = appointmentRepository.findByBloodBankId(bloodBankId);
            log.info(MessageUtils.retrieveSuccess(APPOINTMENTS + " for Blood Bank: " + bloodBankId));
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

    // Update appointment status and award points if completed
    public Appointment updateStatus(String id, String newStatus) {
        try {
            Appointment appointment = getById(id);
            if (appointment == null) {
                throw new ServiceException("Appointment not found", new RuntimeException("Appointment not found"));
            }
            
            String oldStatus = appointment.getStatus();
            appointment.setStatus(newStatus);
            Appointment updatedAppointment = appointmentRepository.save(appointment);
            
            // If status changed to "Complete" or "Completed", award donation points
            if (("Complete".equalsIgnoreCase(newStatus) || "Completed".equalsIgnoreCase(newStatus)) 
                && !("Complete".equalsIgnoreCase(oldStatus) || "Completed".equalsIgnoreCase(oldStatus))) {
                
                String donorId = appointment.getDonorId();
                if (donorId != null && !donorId.isEmpty()) {
                    try {
                        // Award 100 points for completing donation
                        rewardPointsManagementService.awardDonationPoints(donorId, id);
                        log.info("Awarded donation points to donor: " + donorId + " for appointment: " + id);
                    } catch (Exception e) {
                        log.error("Failed to award points to donor: " + donorId, e);
                        // Don't fail the status update if points award fails
                    }
                }
            }
            
            log.info(MessageUtils.updateSuccess(APPOINTMENT + " status"));
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

    /**
     * Automatically mark appointments as "Missed" if the appointment date/time has passed
     * and the status is still "Pending" or "Scheduled"
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000) // Run every hour (3600000 ms = 1 hour)
    public void markMissedAppointments() {
        try {
            Date now = new Date();
            
            // Find all pending/scheduled appointments
            List<Appointment> allAppointments = appointmentRepository.findAll();
            int missedCount = 0;
            
            for (Appointment appointment : allAppointments) {
                // Check if appointment is pending/scheduled and date has passed
                if (appointment.getAppointmentDate() != null && 
                    ("Pending".equalsIgnoreCase(appointment.getStatus()) || 
                     "Scheduled".equalsIgnoreCase(appointment.getStatus()))) {
                    
                    // Add grace period of 2 hours after appointment time
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(appointment.getAppointmentDate());
                    cal.add(Calendar.HOUR, 2);
                    Date appointmentWithGrace = cal.getTime();
                    
                    if (now.after(appointmentWithGrace)) {
                        appointment.setStatus("Missed");
                        appointmentRepository.save(appointment);
                        missedCount++;
                        log.info("Marked appointment " + appointment.getId() + " as Missed");
                    }
                }
            }
            
            if (missedCount > 0) {
                log.info("Marked " + missedCount + " appointments as Missed");
            }
        } catch (Exception e) {
            log.error("Error marking missed appointments: " + e.getMessage(), e);
        }
    }

    /**
     * Get the last completed donation date for a donor
     * @param userId The donor's user ID
     * @return The date of the last completed donation, or null if no completed donations
     */
    public Date getLastCompletedDonationDate(String userId) {
        try {
            List<Appointment> userAppointments = appointmentRepository.findByUserId(userId);
            log.info("Found {} appointments for user {}", userAppointments.size(), userId);
            
            // Log all appointment details for debugging
            for (Appointment apt : userAppointments) {
                log.info("Appointment {}: status={}, appointmentDate={}, visitationDate={}, dateToday={}", 
                        apt.getId(), apt.getStatus(), apt.getAppointmentDate(), 
                        apt.getVisitationDate(), apt.getDateToday());
            }
            
            // Filter for completed appointments and find the most recent one
            Date lastDonationDate = userAppointments.stream()
                .filter(appointment -> "Complete".equalsIgnoreCase(appointment.getStatus()) || 
                                      "Completed".equalsIgnoreCase(appointment.getStatus()))
                .map(appointment -> {
                    // Try appointmentDate first, then visitationDate, then parse dateToday
                    if (appointment.getAppointmentDate() != null) {
                        return appointment.getAppointmentDate();
                    } else if (appointment.getVisitationDate() != null) {
                        return appointment.getVisitationDate();
                    } else if (appointment.getDateToday() != null) {
                        // Try to parse dateToday string to Date
                        try {
                            return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(appointment.getDateToday());
                        } catch (Exception e) {
                            log.warn("Could not parse dateToday: {}", appointment.getDateToday());
                            return null;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .max(Date::compareTo)
                .orElse(null);
            
            log.info("Last donation date for user {}: {}", userId, lastDonationDate);
            return lastDonationDate;
        } catch (Exception e) {
            log.error("Error getting last completed donation date for user: " + userId, e);
            return null;
        }
    }
}


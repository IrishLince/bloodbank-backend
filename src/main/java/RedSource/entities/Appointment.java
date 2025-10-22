package RedSource.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Document(collection = "appointments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Appointment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("donor_id")
    private String donorId;

    @Field("blood_bank_id")
    private String bloodBankId;

    @Field("appointment_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date appointmentDate;

    @Field("status")
    private String status;

    @Field("user_id")
    private String userId;

    @Field("visitation_date")
    private Date visitationDate;

    @Field("notes")
    private String notes;

    // Additional donor and appointment details for richer persistence
    @Field("surname")
    private String surname;

    @Field("first_name")
    private String firstName;

    @Field("middle_initial")
    private String middleInitial;

    @Field("blood_type")
    private String bloodType;

    @Field("date_today")
    private String dateToday;

    @Field("birthday")
    private String birthday;

    @Field("age")
    private String age;

    @Field("sex")
    private String sex;

    @Field("civil_status")
    private String civilStatus;

    @Field("home_address")
    private String homeAddress;

    @Field("phone_number")
    private String phoneNumber;

    @Field("office_phone")
    private String officePhone;

    @Field("occupation")
    private String occupation;

    @Field("patient_name")
    private String patientName;

    @Field("donation_center")
    private String donationCenter;

    @Field("appointment_time")
    private String appointmentTime;

    @Field("medical_history")
    private Map<String, Object> medicalHistory;

    // Explicit getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDonorId() {
        return donorId;
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public String getBloodBankId() {
        return bloodBankId;
    }

    public void setBloodBankId(String bloodBankId) {
        this.bloodBankId = bloodBankId;
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getVisitationDate() {
        return visitationDate;
    }

    public void setVisitationDate(Date visitationDate) {
        this.visitationDate = visitationDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}


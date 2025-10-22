package RedSource.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Document(collection = "deliveries")
public class Delivery implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("request_id")
    private String requestId; // Links to HospitalRequest

    @Field("hospital_name")
    private String hospitalName;

    @Field("blood_bank_name")
    private String bloodBankName;

    @Field("blood_bank_id")
    private String bloodBankId;

    @Field("blood_bank_address")
    private String bloodBankAddress;

    @Field("contact_info")
    private String contactInfo;

    @Field("blood_bank_phone")
    private String bloodBankPhone;

    @Field("blood_bank_email")
    private String bloodBankEmail;

    @Field("items_summary")
    private String itemsSummary; // A summary string of items

    @Field("blood_items")
    private List<BloodInventoryItem> bloodItems; // Detailed list of blood types and units

    @Field("scheduled_date")
    private Date scheduledDate;

    @Field("estimated_time")
    private String estimatedTime;

    @Field("status")
    private String status; // e.g., PENDING, PROCESSING, IN TRANSIT, FULFILLED, CANCELLED, DELAYED

    @Field("priority")
    private String priority;

    @Field("driver_name")
    private String driverName;

    @Field("driver_contact")
    private String driverContact;

    @Field("vehicle_id")
    private String vehicleId;

    @Field("delivered_date")
    private Date deliveredDate;

    @Field("delivered_time")
    private String deliveredTime;

    @Field("notes")
    private String notes;

    @Field("tracking_history")
    private List<TrackingEvent> trackingHistory;

    @Field("created_at")
    private Date createdAt;

    @Field("updated_at")
    private Date updatedAt;
}

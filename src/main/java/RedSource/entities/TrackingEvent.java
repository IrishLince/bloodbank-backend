package RedSource.entities;

import lombok.Data;
import java.util.Date;

@Data
public class TrackingEvent {
    private Date timestamp;
    private String status;
    private String location;
    private String note;
}

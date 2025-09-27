package RedSource.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BloodInventoryItem {
    @JsonProperty("bloodType")
    private String bloodType;
    
    @JsonProperty("units")
    private int units;
}

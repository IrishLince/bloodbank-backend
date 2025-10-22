package RedSource.entities;

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

@Document(collection = "rewards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Reward implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("title")
    private String title;

    @Field("description")
    private String description;

    @Field("points_cost")
    private Integer pointsCost;

    @Field("reward_type")
    private String rewardType; // BADGE, MEDICAL_SERVICE, GIFT_CARD, PRIORITY_BOOKING, BLOOD_BAG_VOUCHER

    @Field("tier")
    private String tier; // NEW, CERTIFIED, BRONZE, SILVER, GOLD

    @Field("auto_unlock")
    private Boolean autoUnlock;

    @Field("unlock_condition")
    private String unlockCondition;

    @Field("image")
    private String image;

    @Field("is_active")
    private Boolean isActive;

    @Field("redeemable_at")
    private String redeemableAt; // HOSPITAL, BLOODBANK, BOTH

    @Field("created_at")
    private Date createdAt;

    @Field("updated_at")
    private Date updatedAt;
}

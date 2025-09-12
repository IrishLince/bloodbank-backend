package RedSource.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Document(collection = "tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    
    @Id
    private String id;
    
    @Field("user_id")
    private String userId;
    
    @Field("token")
    private String token;
    
    @Field("token_type")
    private String tokenType; // "ACCESS" or "REFRESH"
    
    @Field("revoked")
    private boolean revoked;
    
    @Field("expired")
    private boolean expired;
    
    @Field("created_at")
    private Date createdAt;
    
    @Field("expires_at")
    private Date expiresAt;
}

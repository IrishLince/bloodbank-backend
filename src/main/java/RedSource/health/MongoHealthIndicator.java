package RedSource.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongoHealthIndicator implements HealthIndicator {

    private final MongoTemplate mongoTemplate;

    public MongoHealthIndicator(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Health health() {
        try {
            // Try to ping MongoDB
            mongoTemplate.getDb().runCommand(new org.bson.Document("ping", 1));
            return Health.up().withDetail("service", "MongoDB").build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("service", "MongoDB")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
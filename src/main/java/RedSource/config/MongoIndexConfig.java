package RedSource.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Configuration
public class MongoIndexConfig {
    
    private static final Logger logger = Logger.getLogger(MongoIndexConfig.class.getName());
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Bean
    public ApplicationRunner createIndexes() {
        return args -> {
            try {
                // Drop existing conflicting indexes if they exist
                String[] indexesToDrop = {"createdAt", "otp_expiration_index", "otp_5min_ttl_index"};
                
                // Drop existing TTL indexes to avoid conflicts
                for (String indexName : indexesToDrop) {
                    try {
                        mongoTemplate.getCollection("otps").dropIndex(indexName);
                    } catch (Exception e) {
                        // Ignore if index doesn't exist
                    }
                }
                
                // Create new TTL index with 300 seconds (5 minutes) expiration
                Index index = new Index("expiresAt", org.springframework.data.domain.Sort.Direction.ASC)
                    .expire(300, TimeUnit.SECONDS)
                    .named("otp_expiration_index");
                
                mongoTemplate.indexOps("otps").ensureIndex(index);
                
            } catch (Exception e) {
                // Silently handle index creation errors
                // The application can still work without the index, the only impact is that
                // expired documents won't be automatically removed
            }
        };
    }
} 
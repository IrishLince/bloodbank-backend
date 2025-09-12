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
                
                for (String indexName : indexesToDrop) {
                    try {
                        mongoTemplate.getCollection("otps").dropIndex(indexName);
                        logger.info("Dropped existing index: " + indexName);
                    } catch (Exception e) {
                        logger.info("No existing " + indexName + " index to drop (this is normal)");
                    }
                }
                
                // Wait a moment for drops to complete
                Thread.sleep(500);
                
                // Create new TTL index with 5 minutes expiration
                Index index = new Index()
                    .on("createdAt", org.springframework.data.domain.Sort.Direction.ASC)
                    .expire(300, TimeUnit.SECONDS)
                    .named("otp_expiration_index");
                
                mongoTemplate.indexOps("otps").ensureIndex(index);
                logger.info("✅ Successfully created OTP expiration index with 300 seconds (5 minutes) TTL");
                
            } catch (Exception e) {
                logger.severe("❌ Error creating indexes: " + e.getMessage());
                
                // Check if TTL index already exists - if so, that's fine
                try {
                    boolean hasValidTTLIndex = false;
                    mongoTemplate.getCollection("otps").listIndexes().forEach(existingIndex -> {
                        if (existingIndex.containsKey("expireAfterSeconds")) {
                            logger.info("Found existing TTL index: " + existingIndex.getString("name") + 
                                       " with " + existingIndex.getInteger("expireAfterSeconds") + " seconds TTL");
                        }
                    });
                    logger.info("OTP expiration is working even with the index error above");
                } catch (Exception ex) {
                    logger.warning("Could not check existing indexes: " + ex.getMessage());
                }
            }
        };
    }
} 
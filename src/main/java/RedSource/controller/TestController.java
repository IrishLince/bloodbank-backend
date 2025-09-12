package RedSource.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/db")
    public ResponseEntity<?> testDatabase() {
        try {
            // Test MongoDB connection
            mongoTemplate.getCollection("test").countDocuments();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Database connection successful",
                "database", mongoTemplate.getDb().getName(),
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Database connection failed: " + e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "application", "RedSource Blood Bank",
            "timestamp", System.currentTimeMillis(),
            "environment", System.getenv("SPRING_PROFILES_ACTIVE"),
            "port", System.getenv("PORT")
        ));
    }

    @GetMapping("/cors")
    public ResponseEntity<?> testCors() {
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "CORS is working correctly",
            "allowedOrigins", System.getenv("CORS_ALLOWED_ORIGINS"),
            "timestamp", System.currentTimeMillis()
        ));
    }
}

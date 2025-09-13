package RedSource.controller;

import RedSource.entities.Token;
import RedSource.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private TokenService tokenService;
    
    // Add ping endpoint for health check
    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "API is up and running",
            "timestamp", System.currentTimeMillis()
        ));
    }

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

    @GetMapping("/tokens/{userId}")
    public ResponseEntity<?> getTokensForUser(@PathVariable String userId) {
        try {
            List<Token> tokens = tokenService.findAllValidTokensByUser(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("tokenCount", tokens.size());
            response.put("tokens", tokens);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/token-check/{token}")
    public ResponseEntity<?> checkToken(@PathVariable String token) {
        try {
            boolean isValid = tokenService.isTokenValid(token);
            Map<String, Object> response = new HashMap<>();
            response.put("token", token.substring(0, Math.min(token.length(), 20)) + "...");
            response.put("isValid", isValid);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/tokens-count")
    public ResponseEntity<?> getAllTokensCount() {
        try {
            long count = mongoTemplate.getCollection("tokens").countDocuments();
            Map<String, Object> response = new HashMap<>();
            response.put("totalTokens", count);
            response.put("message", "Total tokens in database");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/tokens-sample")
    public ResponseEntity<?> getTokensSample() {
        try {
            List<Token> tokens = tokenService.findAllValidTokens();
            List<Map<String, Object>> sampleTokens = tokens.stream()
                .limit(3)
                .map(token -> {
                    Map<String, Object> tokenInfo = new HashMap<>();
                    tokenInfo.put("createdAt", token.getCreatedAt());
                    tokenInfo.put("tokenPreview", token.getToken().substring(0, Math.min(token.getToken().length(), 30)) + "...");
                    return tokenInfo;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("sampleTokens", sampleTokens);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/clear-tokens")
    public ResponseEntity<?> clearAllTokens() {
        try {
            long count = tokenService.countAllTokens();
            tokenService.deleteAllTokens();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "All tokens cleared successfully");
            response.put("deletedCount", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error clearing tokens: " + e.getMessage());
        }
    }
}

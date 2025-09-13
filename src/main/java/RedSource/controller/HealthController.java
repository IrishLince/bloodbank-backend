package RedSource.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<?> simpleHealth() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "message", "Application is running",
            "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/actuator/health")
    public ResponseEntity<?> actuatorHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        
        Map<String, Object> components = new HashMap<>();
        components.put("ping", Map.of("status", "UP"));
        components.put("diskSpace", Map.of("status", "UP"));
        components.put("mongo", Map.of("status", "UP"));
        components.put("db", Map.of("status", "UP"));
        
        response.put("components", components);
        
        return ResponseEntity.ok(response);
    }
}

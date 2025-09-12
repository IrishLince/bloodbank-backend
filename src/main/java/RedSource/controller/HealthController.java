package RedSource.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "components", Map.of(
                "ping", Map.of("status", "UP")
            )
        ));
    }
}

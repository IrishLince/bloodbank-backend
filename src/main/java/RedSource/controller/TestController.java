package RedSource.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "message", "API is up and running - v2",
            "timestamp", System.currentTimeMillis()
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

package RedSource.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Map;

@Configuration
public class HealthConfig {

    @Bean
    public HealthIndicator pingHealthIndicator() {
        return () -> Health.up().withDetail("ping", "pong").build();
    }


}
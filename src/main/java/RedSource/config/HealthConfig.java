package RedSource.config;

import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthEndpointGroups;
import org.springframework.boot.actuate.health.HealthEndpointGroupsRegistry;
import org.springframework.boot.actuate.health.SimpleStatusAggregator;
import org.springframework.boot.actuate.health.StatusAggregator;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class HealthConfig {

    @Bean
    public StatusAggregator statusAggregator() {
        return new SimpleStatusAggregator(Arrays.asList(
                Status.UP,
                Status.DOWN,
                Status.OUT_OF_SERVICE,
                Status.UNKNOWN));
    }

    @Bean
    public HealthEndpoint healthEndpoint(HealthContributorRegistry registry,
                                        HealthEndpointGroups groups) {
        return new HealthEndpoint(registry, groups);
    }

    @Bean
    public HealthIndicator pingHealthIndicator() {
        return () -> Health.up().withDetail("ping", "pong").build();
    }

    private static class Health {
        private final Status status;
        private final Map<String, Object> details;

        private Health(Status status, Map<String, Object> details) {
            this.status = status;
            this.details = details;
        }

        public static Builder up() {
            return status(Status.UP);
        }

        public static Builder down() {
            return status(Status.DOWN);
        }

        public static Builder status(Status status) {
            return new Builder(status);
        }

        public Status getStatus() {
            return status;
        }

        public Map<String, Object> getDetails() {
            return details;
        }

        public static class Builder {
            private final Status status;
            private final Map<String, Object> details;

            public Builder(Status status) {
                this.status = status;
                this.details = new LinkedHashMap<>();
            }

            public Builder withDetail(String key, Object value) {
                this.details.put(key, value);
                return this;
            }

            public Health build() {
                return new Health(this.status, this.details);
            }
        }
    }
}
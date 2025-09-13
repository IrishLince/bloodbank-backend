package RedSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class AppApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}

	@Component
	public static class StartupReadinessHandler implements ApplicationListener<ContextRefreshedEvent> {
		@Override
		public void onApplicationEvent(ContextRefreshedEvent event) {
			// Add a small delay to ensure MongoDB connection is established
			try {
				Thread.sleep(5000); // 5 second delay
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			
			// Mark application as ready
			ApplicationContext context = event.getApplicationContext();
			AvailabilityChangeEvent.publish(context, ReadinessState.ACCEPTING_TRAFFIC);
		}
	}
}

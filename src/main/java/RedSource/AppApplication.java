package RedSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.beans.factory.annotation.Autowired;
import RedSource.services.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class AppApplication {
	
	private static final Logger logger = LoggerFactory.getLogger(AppApplication.class);
	
	@Autowired
	private TokenService tokenService;

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		try {
			tokenService.cleanupOldTokensWithoutUUID();
		} catch (Exception e) {
			// Silently handle any errors during token cleanup
		}
	}

}

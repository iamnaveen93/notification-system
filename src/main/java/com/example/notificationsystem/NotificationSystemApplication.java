package com.example.notificationsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class NotificationSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationSystemApplication.class, args);
	}

	/**
	 * Request hashing still uses the Jackson 2 API. Spring Boot 4 configures
	 * Jackson 3 for MVC, so expose an explicit Jackson 2 mapper for that
	 * internal use case.
	 */
	@Bean
	ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

}

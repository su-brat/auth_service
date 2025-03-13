package com.status_app.auth_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@Slf4j
public class AuthServiceApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(AuthServiceApplication.class, args);
		String[] activeProfiles = context.getEnvironment().getActiveProfiles();
		log.info("environment: {}", activeProfiles[0]);
	}

}

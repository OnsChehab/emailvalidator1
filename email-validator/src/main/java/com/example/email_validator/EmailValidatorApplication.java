package com.example.email_validator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class EmailValidatorApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(EmailValidatorApplication.class, args);

		// Get the assigned port
		Environment env = context.getEnvironment();
		String port = env.getProperty("local.server.port");
		System.out.println("===========================================");
		System.out.println("Application is running on port: " + port);
		System.out.println("Web interface: http://localhost:" + port);
		System.out.println("H2 Console: http://localhost:" + port + "/h2-console");
		System.out.println("API Endpoint: http://localhost:" + port + "/api/emails");
		System.out.println("===========================================");
	}
}
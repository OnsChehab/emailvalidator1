package com.example.email_validator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class EmailValidatorApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		try {
			// Configuration programmatique du port
			SpringApplication app = new SpringApplication(EmailValidatorApplication.class);
			app.setDefaultProperties(java.util.Collections.singletonMap("server.port", "8099"));

			ConfigurableApplicationContext context = app.run(args);
			Environment env = context.getEnvironment();
			String port = env.getProperty("server.port", "8099");

			System.out.println("===========================================");
			System.out.println("✅ Application démarrée avec succès!");
			System.out.println("📍 Port: " + port);
			System.out.println("🌐 Interface web: http://localhost:" + port);
			System.out.println("🗄️  Console H2: http://localhost:" + port + "/h2-console");
			System.out.println("📚 API Docs: http://localhost:" + port + "/swagger-ui.html");
			System.out.println("===========================================");

		} catch (Exception e) {
			System.err.println("❌ Erreur lors du démarrage de l'application:");
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(EmailValidatorApplication.class);
	}
}
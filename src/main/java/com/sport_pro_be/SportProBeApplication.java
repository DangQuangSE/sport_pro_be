package com.sport_pro_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@PropertySources({
		@PropertySource("classpath:application.properties"),
		@PropertySource(value = "file:.env", ignoreResourceNotFound = true)
})
public class SportProBeApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();
		if (dotenv != null) {
			setIfPresent("APP_JWT_SECRET", dotenv.get("APP_JWT_SECRET"), dotenv.get("JWT_SECRET_KEY"));
			setIfPresent("APP_JWT_EXP_MINUTES", dotenv.get("APP_JWT_EXP_MINUTES"));
			setIfPresent("APP_OTP_EXP_MINUTES", dotenv.get("APP_OTP_EXP_MINUTES"));
			setIfPresent("APP_OTP_RESEND_SECONDS", dotenv.get("APP_OTP_RESEND_SECONDS"));
			setIfPresent("APP_FORGOT_PASSWORD_TOKEN_EXP_MINUTES", dotenv.get("APP_FORGOT_PASSWORD_TOKEN_EXP_MINUTES"));
			setIfPresent("APP_FORGOT_PASSWORD_MAX_ATTEMPTS", dotenv.get("APP_FORGOT_PASSWORD_MAX_ATTEMPTS"));
			setIfPresent("DB_HOST", dotenv.get("DB_HOST"));
			setIfPresent("DB_PORT", dotenv.get("DB_PORT"));
			setIfPresent("DB_NAME", dotenv.get("DB_NAME"));
			setIfPresent("DB_USERNAME", dotenv.get("DB_USERNAME"));
			setIfPresent("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
			setIfPresent("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD"));
			setIfPresent("APP_MAIL_FROM", dotenv.get("APP_MAIL_FROM"), dotenv.get("MAIL_FROM"));
			setIfPresent("MAIL_HOST", dotenv.get("MAIL_HOST"));
			setIfPresent("MAIL_PORT", dotenv.get("MAIL_PORT"));
			setIfPresent("MAIL_SMTP_AUTH", dotenv.get("MAIL_SMTP_AUTH"));
			setIfPresent("MAIL_SMTP_STARTTLS", dotenv.get("MAIL_SMTP_STARTTLS"));
			setIfPresent("CLOUDINARY_CLOUD_NAME", dotenv.get("CLOUDINARY_CLOUD_NAME"));
			setIfPresent("CLOUDINARY_API_KEY", dotenv.get("CLOUDINARY_API_KEY"));
			setIfPresent("CLOUDINARY_API_SECRET", dotenv.get("CLOUDINARY_API_SECRET"));
		}
		SpringApplication.run(SportProBeApplication.class, args);
	}

	private static void setIfPresent(String key, String... candidates) {
		if (candidates == null) {
			return;
		}
		for (String value : candidates) {
			if (value != null && !value.isBlank()) {
				System.setProperty(key, value);
				return;
			}
		}
	}

}

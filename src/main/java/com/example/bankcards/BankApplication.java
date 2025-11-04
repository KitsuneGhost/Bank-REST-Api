package com.example.bankcards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Main entry point for the Bank REST API application.
 * <p>
 * This class bootstraps the Spring Boot context, initializing all
 * components, configurations, and services defined across the project.
 * <p>
 * The {@link SpringBootApplication} annotation enables:
 * <ul>
 *   <li>Component scanning within the {@code com.example.bankcards} package</li>
 *   <li>Auto-configuration of Spring Boot modules</li>
 *   <li>Support for external configuration via {@code application.yml}</li>
 * </ul>
 *
 * <p><b>Usage:</b>
 * <pre>
 * java -jar bankcards.jar
 * </pre>
 *
 * <p>This application exposes RESTful endpoints for managing users, cards,
 * and transactions, secured by JWT-based authentication.
 *
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.SpringApplication
 */
@SpringBootApplication
public class BankApplication {

    /**
     * Launches the Spring Boot application.
     *
     * @param args optional command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(BankApplication.class, args);
    }
}
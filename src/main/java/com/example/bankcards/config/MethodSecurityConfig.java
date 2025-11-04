package com.example.bankcards.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;


/**
 * Security configuration class that enables method-level access control
 * using Spring Security annotations such as {@link org.springframework.security.access.prepost.PreAuthorize}.
 * <p>
 * The {@link EnableMethodSecurity} annotation activates Spring’s AOP-based
 * authorization checks on service and controller methods, allowing
 * fine-grained role and permission validation directly at the method level.
 * <p>
 * Typical usage includes annotations like:
 * <pre>
 * {@code
 * @PreAuthorize("hasRole('ADMIN')")
 * public void deleteUser(Long id) { ... }
 * }
 * </pre>
 *
 * <p>
 * This configuration does not define any beans but serves as a trigger
 * for Spring Security’s method security subsystem.
 *
 * @see org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
 * @see org.springframework.security.access.prepost.PreAuthorize
 * @see org.springframework.security.access.prepost.PostAuthorize
 */
@Configuration
@EnableMethodSecurity // enables @PreAuthorize
public class MethodSecurityConfig {}

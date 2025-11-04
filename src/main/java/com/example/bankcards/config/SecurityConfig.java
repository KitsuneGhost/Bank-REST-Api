package com.example.bankcards.config;

import com.example.bankcards.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * Central Spring Security configuration for the Bank REST API.
 * <p>
 * Defines the HTTP security filter chain, authentication manager, and password encoder.
 * <p>
 * This configuration enforces stateless JWT-based authentication,
 * disables CSRF (since sessions are not used), and exposes selected
 * public endpoints for authentication and OpenAPI documentation.
 * <p>
 * The {@link org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity}
 * annotation enables method-level access control annotations such as
 * {@link org.springframework.security.access.prepost.PreAuthorize}.
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>JWT-based authentication with {@code JwtAuthenticationFilter}</li>
 *   <li>Stateless session policy for RESTful design</li>
 *   <li>Role-based access control integrated with {@code @PreAuthorize}</li>
 *   <li>Exposed endpoints for Swagger and authentication routes</li>
 * </ul>
 *
 * @see JwtAuthenticationFilter
 * @see org.springframework.security.web.SecurityFilterChain
 * @see org.springframework.security.authentication.AuthenticationManager
 * @see org.springframework.security.crypto.password.PasswordEncoder
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Configures the HTTP security filter chain for JWT-based authentication.
     * <p>
     * This setup disables CSRF protection (not needed for stateless APIs),
     * enforces a stateless session policy, defines public endpoints,
     * and requires authentication for all other requests.
     * <p>
     * The provided {@link JwtAuthenticationFilter} is registered before
     * the {@link UsernamePasswordAuthenticationFilter} to intercept and validate JWT tokens.
     *
     * @param http       the {@link HttpSecurity} builder
     * @param jwtFilter  custom filter that processes JWT tokens from incoming requests
     * @return configured {@link SecurityFilterChain} instance
     * @throws Exception if the security configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Exposes the {@link AuthenticationManager} bean from the application's
     * {@link AuthenticationConfiguration}.
     * <p>
     * This allows other components (e.g., authentication controllers or filters)
     * to perform manual authentication using the configured authentication providers.
     *
     * @param config Spring Security authentication configuration
     * @return shared {@link AuthenticationManager} instance
     * @throws Exception if the authentication manager cannot be created
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Provides the {@link PasswordEncoder} used for hashing user passwords.
     * <p>
     * Uses {@link org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder}
     * to ensure strong, salted one-way password hashing compatible with Spring Security.
     *
     * @return configured {@link PasswordEncoder} instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

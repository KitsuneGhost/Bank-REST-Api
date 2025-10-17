package com.example.bankcards.config;

import com.example.bankcards.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ---- PUBLIC ----
                        .requestMatchers("/auth/**").permitAll()
                        // Swagger/OpenAPI
                        //.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // ---- CARDS (CardController) ----
                        // list/get cards -> USER & ADMIN (but USER must only see OWN cards → enforced in service)
                        .requestMatchers(HttpMethod.GET, "/cards").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.GET, "/cards/*").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.GET, "/cards/filter").hasAnyRole("USER","ADMIN")

                        // sensitive: search by card number → ADMIN only
                        .requestMatchers(HttpMethod.GET, "/cards/search/**").hasRole("ADMIN")

                        // create card for user -> ADMIN only
                        .requestMatchers(HttpMethod.POST, "/cards/users/*").hasRole("ADMIN")

                        // update card -> ADMIN only
                        .requestMatchers(HttpMethod.PUT, "/cards/*").hasRole("ADMIN")

                        // delete card -> ADMIN only
                        .requestMatchers(HttpMethod.DELETE, "/cards/*").hasRole("ADMIN")

                        // ---- USERS (UserController) ----
                        // get a user's cards -> ADMIN or the owner (role rule allows both, owner check in service)
                        .requestMatchers(HttpMethod.GET, "/users/*/cards").hasAnyRole("USER","ADMIN")

                        // list users -> ADMIN only
                        .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")

                        // create/update/delete users via /users -> ADMIN only
                        .requestMatchers(HttpMethod.POST, "/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/users/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/*").hasRole("ADMIN")

                        // filters on /users -> ADMIN only (they leak PII as of now)
                        .requestMatchers("/users/filter/**").hasRole("ADMIN")

                        // get user by id -> ADMIN or the owner (owner check in service)
                        .requestMatchers(HttpMethod.GET, "/users/*").hasAnyRole("USER","ADMIN")

                        // ---- EVERYTHING ELSE ----
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

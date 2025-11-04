package com.example.bankcards.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Configuration class for OpenAPI (Swagger) documentation.
 * <p>
 * Defines the metadata and security scheme used to generate the interactive
 * API documentation via Swagger UI.
 * <p>
 * This configuration registers a {@link io.swagger.v3.oas.models.OpenAPI} bean
 * that exposes the API specification at the default OpenAPI endpoint
 * (typically {@code /v3/api-docs}) and provides a JWT-based
 * authentication mechanism for secured endpoints.
 *
 * <p><b>Features:</b>
 * <ul>
 *   <li>Sets API title and version</li>
 *   <li>Registers a bearer token authentication scheme ({@code Authorization: Bearer &lt;JWT&gt;})</li>
 *   <li>Integrates security requirements into the global OpenAPI spec</li>
 * </ul>
 *
 * @see io.swagger.v3.oas.models.OpenAPI
 * @see io.swagger.v3.oas.models.security.SecurityScheme
 * @see io.swagger.v3.oas.models.security.SecurityRequirement
 * @see org.springdoc.core.annotations.RouterOperation
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates and configures the {@link io.swagger.v3.oas.models.OpenAPI} instance
     * used by SpringDoc to generate the API documentation.
     * <p>
     * The configuration includes:
     * <ul>
     *   <li>Basic API metadata (title and version)</li>
     *   <li>A bearer-based {@link io.swagger.v3.oas.models.security.SecurityScheme}
     *       for JWT authentication</li>
     *   <li>Global {@link io.swagger.v3.oas.models.security.SecurityRequirement}
     *       referencing the security scheme</li>
     * </ul>
     *
     * @return configured {@link io.swagger.v3.oas.models.OpenAPI} object
     */
    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info().title("Bank REST API").version("1.0.0"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
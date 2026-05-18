package edu.uniquindio.stayhub_v2.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI 3.0 (Swagger) documentation.
 *
 * <p>This class configures the OpenAPI specification for the StayHub API,
 * providing comprehensive documentation that is exposed through Swagger UI.
 * It defines API metadata, server environments, and security schemes for
 * JWT-based authentication.</p>
 *
 * <p><b>Features Configured:</b></p>
 * <ul>
 *   <li>API metadata (title, version, description)</li>
 *   <li>Contact and license information</li>
 *   <li>Server definitions for different environments</li>
 *   <li>JWT Bearer authentication scheme for secured endpoints</li>
 * </ul>
 *
 * <p><b>Accessing the Documentation:</b></p>
 * Once the application is running, the Swagger UI can be accessed at:
 * <ul>
 *   <li><a href="http://localhost:8080/swagger-ui.html">http://localhost:8080/swagger-ui.html</a></li>
 *   <li><a href="http://localhost:8080/swagger-ui/index.html">http://localhost:8080/swagger-ui/index.html</a></li>
 * </ul>
 *
 * <p>The OpenAPI JSON specification is available at:
 * <ul>
 *   <li><a href="http://localhost:8080/v3/api-docs">http://localhost:8080/v3/api-docs</a></li>
 * </ul>
 *
 * <p><b>Using JWT Authentication in Swagger UI:</b></p>
 * <ol>
 *   <li>Obtain a JWT token from the authentication endpoint</li>
 *   <li>Click the "Authorize" button in Swagger UI</li>
 *   <li>Enter the token in the format: {@code Bearer <your-token>}</li>
 *   <li>All subsequent requests will include the Authorization header</li>
 * </ol>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see OpenAPIDefinition
 * @see OpenAPI
 * @see io.swagger.v3.oas.annotations.security.SecurityScheme
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "StayHub v2",
                version = "1.0.0",
                description = """
                        REST API documentation for StayHub v2 - Accommodation Booking Platform.
                        
                        ## Overview
                        StayHub is a platform that connects guests with hosts for short-term accommodation rentals.
                        
                        ## Key Features
                        - User registration and authentication
                        - Accommodation listing and search
                        - Reservation management
                        - Host dashboard and management tools
                        
                        ## Authentication
                        Most endpoints require JWT Bearer token authentication.
                        Use the `/api/auth/login` endpoint to obtain a token.
                        """,
                contact = @Contact(
                        name = "Esteban Gómez León",
                        email = "estebangumy05@gmail.com"
                ),
                license = @License(
                        name = "GNU GPL V3.0",
                        url = "https://www.gnu.org/licenses/gpl-3.0.html"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Local development server"
                ),
                @Server(
                        url = "https://stayhub-v2.onrender.com",
                        description = "Production server"
                )
        }
)
public class OpenApiConfig {

    /**
     * Creates and configures the OpenAPI bean with custom security schemes.
     *
     * <p>This bean defines the security components used by the API documentation,
     * specifically the JWT Bearer authentication scheme. The security scheme
     * is globally available and can be referenced by any endpoint using the
     * {@code @SecurityRequirement} annotation.</p>
     *
     * <p><b>Security Scheme Details:</b></p>
     * <ul>
     *   <li><b>Type:</b> HTTP Authentication</li>
     *   <li><b>Scheme:</b> Bearer</li>
     *   <li><b>Bearer Format:</b> JWT (JSON Web Token)</li>
     *   <li><b>Scheme Name:</b> "Bearer Authentication" (used as reference key)</li>
     * </ul>
     *
     * <p><b>Usage in Controllers:</b></p>
     * To secure an endpoint with this authentication scheme, use:
     * <pre>{@code
     * @Operation(security = @SecurityRequirement(name = "Bearer Authentication"))
     * @GetMapping("/secured-endpoint")
     * public ResponseEntity<?> securedEndpoint() {
     *     // Endpoint logic
     * }
     * }</pre>
     *
     * <p><b>Or globally for all endpoints in a controller:</b></p>
     * <pre>{@code
     * @RestController
     * @SecurityRequirement(name = "Bearer Authentication")
     * public class SecuredController {
     *     // All endpoints require authentication
     * }
     * }</pre>
     *
     * @return A configured OpenAPI instance with JWT Bearer security scheme
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("""
                                                Enter your JWT token for authentication.
                                                
                                                **Format:** `Bearer <your-jwt-token>`
                                                
                                                **Obtaining a token:**
                                                Use the `/api/auth/login` endpoint with valid credentials.
                                                """)
                        )
                );
    }
}
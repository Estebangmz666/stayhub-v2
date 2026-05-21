package edu.uniquindio.stayhub_v2.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration class for the StayHub application.
 *
 * <p>This class configures all security-related aspects of the application, including:
 * <ul>
 *   <li>JWT-based stateless authentication</li>
 *   <li>Password encoding using BCrypt</li>
 *   <li>CORS configuration for frontend integration</li>
 *   <li>URL-based authorization rules</li>
 *   <li>Session management (stateless for JWT)</li>
 *   <li>CSRF protection (disabled for REST APIs)</li>
 * </ul>
 *
 * <p><b>Security Architecture:</b></p>
 * The application uses a stateless JWT-based authentication flow:
 * <ol>
 *   <li>Client authenticates via {@code /api/v2/users/auth/**} endpoints</li>
 *   <li>Server returns a signed JWT token</li>
 *   <li>Client includes the token in the {@code Authorization: Bearer <token>} header</li>
 *   <li>{@link JwtAuthenticationFilter} validates the token on each request</li>
 *   <li>Security context is established for the authenticated user</li>
 * </ol>
 *
 * <p><b>Public Endpoints (No Authentication Required):</b></p>
 * <ul>
 *   <li>Swagger UI and API documentation</li>
 *   <li>Authentication endpoints (login, register, password recovery)</li>
 *   <li>OPTIONS preflight requests for CORS</li>
 * </ul>
 *
 * <p><b>Protected Endpoints:</b></p>
 * All other endpoints require a valid JWT token in the Authorization header.
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see JwtAuthenticationFilter
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${stayhub.frontend.url}")
    private String frontendUrl;

    /**
     * Provides a password encoder bean for secure password hashing.
     *
     * <p>Uses BCrypt hashing algorithm which is:
     * <ul>
     *   <li>Adaptive and resistant to brute-force attacks</li>
     *   <li>Automatically generates and stores the salt within the hash</li>
     *   <li>Industry standard for password storage</li>
     * </ul>
     *
     * <p>The default strength factor is 10 (2^10 iterations), providing
     * a good balance between security and performance.</p>
     *
     * @return BCryptPasswordEncoder instance for password encoding/validation
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the security filter chain for HTTP requests.
     *
     * <p>This is the core security configuration that defines:
     * <ul>
     *   <li>URL-based access control rules</li>
     *   <li>Session management strategy (stateless)</li>
     *   <li>CSRF protection settings</li>
     *   <li>JWT filter integration</li>
     * </ul>
     *
     * <p><b>Filter Order:</b></p>
     * The {@link JwtAuthenticationFilter} is executed before
     * {@link UsernamePasswordAuthenticationFilter} to establish
     * authentication context from the JWT token.
     *
     * <p><b>Authorization Rules (in order of evaluation):</b></p>
     * <ol>
     *   <li>OPTIONS requests → Public (CORS preflight)</li>
     *   <li>Swagger UI resources → Public (API documentation)</li>
     *   <li>Authentication endpoints → Public (login/register)</li>
     *   <li>All other endpoints → Authenticated</li>
     * </ol>
     *
     * @param http The HttpSecurity instance to configure
     * @return The built SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                // Enable CORS with custom configuration
                .cors(Customizer.withDefaults())

                // Disable CSRF for stateless REST API (JWT provides protection)
                .csrf(AbstractHttpConfigurer::disable)

                // Configure stateless session management (no HTTP sessions)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Define authorization rules for HTTP requests
                .authorizeHttpRequests(auth -> auth
                        // Allow all OPTIONS requests (CORS preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public access to Swagger UI and API documentation
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/openapi.yaml",
                                "/actuator",
                                "/actuator/**"

                        ).permitAll()

                        // Public access to authentication endpoints
                        .requestMatchers("/api/v2/users/auth/**").permitAll()

                        // Admin-only endpoints
                        .requestMatchers("/api/v2/admin-actions/**").hasRole("ADMIN")

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) for the application.
     *
     * <p>This configuration allows the frontend application running on
     * different origins to communicate with the API. It defines:
     * <ul>
     *   <li>Allowed origins (frontend URLs)</li>
     *   <li>Allowed HTTP methods</li>
     *   <li>Allowed headers</li>
     *   <li>Credentials support for authenticated requests</li>
     * </ul>
     *
     * <p><b>Security Considerations:</b></p>
     * In production, replace wildcard origins and headers with specific values.
     * The current configuration allows:
     * <ul>
     *   <li>Origins: {@code http://localhost:3000}, {@code http://127.0.0.1:3000}</li>
     *   <li>Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS</li>
     *   <li>Headers: All (wildcard)</li>
     *   <li>Credentials: Enabled (allows cookies and Authorization headers)</li>
     * </ul>
     *
     * <p><b>Production Example:</b></p>
     * <pre>{@code
     * config.setAllowedOrigins(List.of("https://app.stayhub.com"));
     * config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
     * }</pre>
     *
     * @return CorsConfigurationSource with the defined CORS rules
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Frontend origins allowed accessing the API
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                frontendUrl
        ));

        // HTTP methods allowed for cross-origin requests
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Headers allowed in requests (wildcard for development)
        config.setAllowedHeaders(List.of("*"));

        // Allow credentials (cookies, Authorization headers) in cross-origin requests
        config.setAllowCredentials(true);

        // Apply CORS configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
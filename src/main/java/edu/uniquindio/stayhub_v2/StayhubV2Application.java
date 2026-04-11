package edu.uniquindio.stayhub_v2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main entry point for the StayHub v2 application.
 *
 * <p>This class bootstraps the Spring Boot application and enables
 * essential framework features. It serves as the foundation for the
 * entire StayHub accommodation booking platform.</p>
 *
 * <p><b>Enabled Features:</b></p>
 * <ul>
 *   <li><b>Spring Boot Auto-Configuration:</b> Automatically configures
 *       Spring components based on classpath dependencies</li>
 *   <li><b>Component Scanning:</b> Scans for Spring components in the
 *       {@code edu.uniquindio.stayhub_v2} package and subpackages</li>
 *   <li><b>JPA Auditing:</b> Enables automatic population of
 *       {@code @CreatedDate} and {@code @LastModifiedDate} fields in
 *       entities extending {@code Auditable}</li>
 * </ul>
 *
 * <p><b>Startup Process:</b></p>
 * <ol>
 *   <li>Spring Boot initializes the application context</li>
 *   <li>Component scanning discovers all beans (Services, Repositories, etc.)</li>
 *   <li>Autoconfiguration sets up Database, Security, Mail, etc.</li>
 *   <li>Embedded Tomcat server starts on the configured port (default: 8080)</li>
 *   <li>Application is ready to serve requests</li>
 * </ol>
 *
 * <p><b>Access Points After Startup:</b></p>
 * <table border="1">
 *   <tr><th>Service</th><th>URL</th></tr>
 *   <tr><td>REST API</td><td><a href="http://localhost:8080/api">http://localhost:8080/api</a></td></tr>
 *   <tr><td>Swagger UI</td><td><a href="http://localhost:8080/swagger-ui.html">http://localhost:8080/swagger-ui.html</a></td></tr>
 *   <tr><td>OpenAPI JSON</td><td><a href="http://localhost:8080/v3/api-docs">http://localhost:8080/v3/api-docs</a></td></tr>
 * </table>
 *
 * <p><b>Running the Application:</b></p>
 * <pre>{@code
 * // Using Maven
 * mvn spring-boot:run
 *
 * // Using Java
 * java -jar target/stayhub-v2.jar
 *
 * // With custom profile
 * java -jar target/stayhub-v2.jar --spring.profiles.active=prod
 * }</pre>
 *
 * <p><b>Required Configuration Files:</b></p>
 * <ul>
 *   <li>{@code application.yml} - Main configuration</li>
 *   <li>{@code application-dev.yml} - Development profile</li>
 *   <li>{@code application-prod.yml} - Production profile</li>
 * </ul>
 *
 * <p><b>Key Dependencies:</b></p>
 * <ul>
 *   <li>Spring Boot Starter Web (REST API)</li>
 *   <li>Spring Boot Starter Data JPA (Database)</li>
 *   <li>Spring Boot Starter Security (Authentication)</li>
 *   <li>Spring Boot Starter Mail (Email notifications)</li>
 *   <li>PostgreSQL (Database, configured in properties)</li>
 *   <li>JWT (Stateless authentication)</li>
 *   <li>Swagger/OpenAPI (API Documentation)</li>
 *   <li>MapStruct (Object mapping)</li>
 *   <li>Thymeleaf (Email templates)</li>
 * </ul>
 *
 * <p><b>Architecture Overview:</b></p>
 * <pre>
 * stayhub_v2/
 * ├── config/       → Spring configuration classes
 * ├── controller/   → REST controllers
 * ├── service/      → Business logic
 * ├── repository/   → Data access layer
 * ├── model/        → JPA entities
 * ├── dto/          → Data Transfer Objects
 * ├── mapper/       → MapStruct mappers
 * ├── exception/    → Custom exceptions
 * └── event/        → Application events
 * </pre>
 *
 * <p><b>Environment Variables:</b></p>
 * <ul>
 *   <li>{@code JWT.SECRET.KEY} - Secret key for JWT signing (min 32 chars)</li>
 *   <li>{@code JWT.TIME.EXPIRATION} - Token expiration in milliseconds</li>
 *   <li>{@code DB_URL} - Database connection URL</li>
 *   <li>{@code DB_USERNAME} - Database username</li>
 *   <li>{@code DB_PASSWORD} - Database password</li>
 *   <li>{@code MAIL_USERNAME} - Email service username</li>
 *   <li>{@code MAIL_PASSWORD} - Email service password</li>
 * </ul>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see SpringBootApplication
 * @see EnableJpaAuditing
 * @see org.springframework.boot.SpringApplication
 */
@EnableJpaAuditing
@SpringBootApplication
public class StayhubV2Application {

	/**
	 * The main entry point for the Spring Boot application.
	 *
	 * <p>This method bootstraps the entire Spring application context,
	 * initializes all beans, starts the embedded web server, and makes
	 * the application ready to handle HTTP requests.</p>
	 *
	 * <p><b>Execution Flow:</b></p>
	 * <ol>
	 *   <li>{@code SpringApplication.run()} creates the application context</li>
	 *   <li>Component scanning discovers all Spring-managed beans</li>
	 *   <li>Autoconfiguration applies based on classpath dependencies</li>
	 *   <li>Database connection pool is initialized</li>
	 *   <li>JPA entity manager factory is created</li>
	 *   <li>Spring Security filter chain is configured</li>
	 *   <li>Embedded Tomcat starts listening on configured port</li>
	 *   <li>Application is ready to serve requests</li>
	 * </ol>
	 *
	 * <p><b>Startup Logs Example:</b></p>
	 * <pre>
	 *   .   ____          _            __ _ _
	 *  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
	 * ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
	 *  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
	 *   '  |____| .__|_| |_|_| |_\__, | / / / /
	 *  =========|_|==============|___/=/_/_/_/
	 *
	 *  :: Spring Boot ::                (v3.2.0)
	 *
	 * 2025-04-10 10:00:00.000  INFO --- StayhubV2Application : Started StayhubV2Application in 5.2 seconds
	 * </pre>
	 *
	 * <p><b>Exit Codes:</b></p>
	 * <ul>
	 *   <li>{@code 0} - Normal termination</li>
	 *   <li>{@code 1} - Application failed to start (configuration error, port in use, etc.)</li>
	 * </ul>
	 *
	 * @param args Command line arguments passed to the application
	 * @see SpringApplication#run(Class, String...)
	 */
	public static void main(String[] args) {
		SpringApplication.run(StayhubV2Application.class, args);
	}
}
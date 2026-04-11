package edu.uniquindio.stayhub_v2.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a user in the StayHub platform.
 *
 * <p>This entity stores all personal, authentication, and profile information
 * for both guests and hosts. Users are the core identity in the system,
 * authenticating via email and password, and possessing roles that determine
 * their capabilities within the platform.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Email-based authentication with unique email constraint</li>
 *   <li>Role-based access control via {@link Role} enum</li>
 *   <li>Soft delete support via {@code deleted} flag</li>
 *   <li>Password recovery mechanism with time-limited codes</li>
 *   <li>Audit trail inherited from {@link Auditable}</li>
 *   <li>Profile picture support via URL</li>
 * </ul>
 *
 * <p><b>Database Indexes:</b></p>
 * <ul>
 *   <li>{@code idx_user_email} - Unique index on email for fast lookups and uniqueness enforcement</li>
 * </ul>
 *
 * <p><b>Relationships:</b></p>
 * <ul>
 *   <li><b>Accommodations (as Host):</b> One-to-Many - Properties listed by this user</li>
 *   <li><b>Reservations (as Guest):</b> One-to-Many - Bookings made by this user</li>
 *   <li><b>Roles:</b> ElementCollection - Set of roles assigned to the user</li>
 * </ul>
 *
 * <p><b>Security Considerations:</b></p>
 * <ul>
 *   <li>Password must be stored using BCrypt or similar strong hashing algorithm</li>
 *   <li>Password recovery codes expire after a configured time period</li>
 *   <li>Soft-deleted users cannot authenticate but their data is preserved</li>
 *   <li>Email is used as the primary identifier for authentication</li>
 * </ul>
 *
 * <p><b>Lifecycle:</b></p>
 * Users support soft deletion. When {@code deleted = true}, the user cannot
 * authenticate, but their historical data (reservations, reviews, listed
 * accommodations) remains in the system for referential integrity.</p>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see Auditable
 * @see Role
 * @see Accommodation
 * @see Reservation
 */
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email", unique = true)
        })
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends Auditable {

    /**
     * The unique identifier for the user.
     * Generated automatically using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user's email address.
     *
     * <p>This is the primary identifier for authentication and communication.
     * Email addresses must be unique across all users (including soft-deleted
     * users to prevent account hijacking).</p>
     *
     * <p><b>Validation Requirements:</b></p>
     * <ul>
     *   <li>Must be unique in the system</li>
     *   <li>Must be a valid email format (e.g., user@domain.com)</li>
     *   <li>Cannot be null</li>
     * </ul>
     *
     * <p><b>Usage:</b></p>
     * <ul>
     *   <li>Login/authentication</li>
     *   <li>Password recovery</li>
     *   <li>Notification emails</li>
     *   <li>Booking confirmations</li>
     * </ul>
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * The user's hashed password.
     *
     * <p><b>⚠️ Security Critical:</b> This field must NEVER store plain text
     * passwords. Always use a strong password encoder (e.g., BCrypt) before
     * persisting to the database.</p>
     *
     * <p><b>Password Requirements:</b></p>
     * <ul>
     *   <li>Minimum 8 characters</li>
     *   <li>At least one uppercase letter</li>
     *   <li>At least one number</li>
     *   <li>Should not be stored in plain text anywhere</li>
     *   <li>Should not be logged or exposed in API responses</li>
     * </ul>
     *
     * <p><b>Hashing Algorithm:</b> BCrypt (via {@code BCryptPasswordEncoder})</p>
     */
    @Column(nullable = false)
    private String password;

    /**
     * The set of roles assigned to the user.
     *
     * <p>Roles determine what actions the user can perform within the platform.
     * A user can have multiple roles simultaneously (e.g., a user who both
     * travels as a guest and lists properties as a host).</p>
     *
     * <p><b>Storage:</b> Roles are stored in a separate table {@code user_roles}
     * using {@code @ElementCollection}. Each role is stored as a string using
     * its enum name.</p>
     *
     * <p><b>Available Roles:</b></p>
     * <ul>
     *   <li>{@link Role#GUEST} - Can search and book accommodations</li>
     *   <li>{@link Role#HOST} - Can list and manage accommodations</li>
     * </ul>
     *
     * <p><b>Default Value:</b> Empty set (roles must be assigned explicitly)</p>
     *
     * <p><b>Usage in Spring Security:</b></p>
     * <pre>{@code
     * // Roles are prefixed with "ROLE_" for Spring Security
     * // A user with Role.HOST gets authority "ROLE_HOST"
     * }</pre>
     *
     * @see Role
     */
    @ElementCollection(targetClass = Role.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * The user's full name as displayed on the platform.
     *
     * <p>Used for personalization in emails, on reservation details, and in
     * profile displays. This should be the user's real name or preferred
     * display name.</p>
     *
     * <p><b>Examples:</b> "John Doe", "María González", "Alex Smith"</p>
     */
    @Column(nullable = false)
    private String fullName;

    /**
     * The user's contact phone number.
     *
     * <p>Used for important communications, emergency contacts during stays,
     * and account verification. The platform expects numbers in Colombian
     * format.</p>
     *
     * <p><b>Format:</b> +57 followed by 10 digits</p>
     * <p><b>Example:</b> +573001234567</p>
     *
     * <p><b>Validation Pattern:</b> {@code ^\+57\d{10}$}</p>
     */
    @Column(nullable = false)
    private String phoneNumber;

    /**
     * The user's date of birth.
     *
     * <p>Used for age verification and legal compliance. Users must be at least
     * 18 years old to create an account on the platform.</p>
     *
     * <p><b>Validation:</b> Must be a date in the past and indicate age ≥ 18</p>
     */
    @Column(nullable = false)
    private LocalDate birthDate;

    /**
     * Soft-delete flag indicating whether this user account is deactivated.
     *
     * <p>When {@code true}, the user cannot authenticate and their profile is
     * hidden from public views. However, their data is preserved for historical
     * records and referential integrity.</p>
     *
     * <p><b>Default:</b> {@code false} (active account)</p>
     *
     * <p><b>Effects of Deletion:</b></p>
     * <ul>
     *   <li>User cannot log in</li>
     *   <li>User's accommodations are hidden (if host)</li>
     *   <li>Past reservations remain visible for audit purposes</li>
     *   <li>Email address remains reserved (cannot be reused)</li>
     * </ul>
     *
     * <p><b>Recovery:</b> Administrators can restore soft-deleted accounts
     * by setting this flag back to {@code false}.</p>
     */
    @Column(name = "deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean deleted = false;

    /**
     * URL to the user's profile picture.
     *
     * <p>Optional field that stores a reference to the user's avatar image.
     * The image is typically stored in a cloud storage service (e.g., AWS S3,
     * Cloudinary) and this field contains the public URL.</p>
     *
     * <p><b>Format:</b> Valid HTTPS URL</p>
     * <p><b>Example:</b> {@code https://cdn.stayhub.com/profiles/user123.jpg}</p>
     *
     * <p>If not provided, a default avatar is displayed in the UI.</p>
     */
    @Column
    private String profilePicture;

    /**
     * Six-digit code for password recovery.
     *
     * <p>Generated when a user requests a password reset. This code is sent to
     * the user's email address and must be provided to verify ownership before
     * allowing a password change.</p>
     *
     * <p><b>Characteristics:</b></p>
     * <ul>
     *   <li>Length: Exactly 6 characters</li>
     *   <li>Format: Alphanumeric or numeric only</li>
     *   <li>Temporary: Valid only until {@link #passwordRecoveryExpiration}</li>
     *   <li>Single-use: Cleared after successful password reset</li>
     * </ul>
     *
     * <p><b>Security:</b> Store hashed in production, though currently stored
     * as plain text for simplicity. Consider using {@code BCrypt} for recovery
     * codes as well.</p>
     *
     * @see #passwordRecoveryExpiration
     */
    @Column(name = "password_recovery_code", length = 6)
    private String passwordRecoveryCode;

    /**
     * Expiration timestamp for the password recovery code.
     *
     * <p>Defines the validity period for the {@link #passwordRecoveryCode}.
     * After this timestamp, the code is considered expired and cannot be used
     * for password recovery.</p>
     *
     * <p><b>Typical Validity Period:</b> 15-30 minutes</p>
     *
     * <p><b>Usage:</b></p>
     * <pre>{@code
     * if (user.getPasswordRecoveryExpiration().isBefore(LocalDateTime.now())) {
     *     throw new InvalidRecoveryCodeException("Recovery code has expired");
     * }
     * }</pre>
     *
     * <p><b>Cleanup:</b> Should be cleared along with the recovery code after
     * successful password reset or expiration.</p>
     *
     * @see #passwordRecoveryCode
     */
    @Column(name = "password_recovery_expiration")
    private LocalDateTime passwordRecoveryExpiration;

    /*
     * TO-DO: CAMBIAR RELACIÓN: USUARIO NO TIENE PROPIEDAD, PROPIEDAD ES DE USUARIO
     *
     * Nota: La relación actual es correcta según el modelo de dominio.
     * Accommodation tiene una referencia @ManyToOne a User (host), lo cual es
     * la forma correcta de modelar que un host puede tener múltiples propiedades.
     *
     * Si se desea navegación bidireccional, se puede agregar:
     *
     * @OneToMany(mappedBy = "host")
     * private Set<Accommodation> accommodations = new HashSet<>();
     *
     * Sin embargo, esto puede causar problemas de rendimiento si un host tiene
     * muchas propiedades. Se recomienda usar consultas específicas en el
     * repositorio en lugar de cargar la colección completa.
     */
    // private Set<Accommodation> accommodations = new HashSet<>();
}
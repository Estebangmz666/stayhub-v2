package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.config.JwtAuthenticationFilter;
import edu.uniquindio.stayhub_v2.dto.auth.ChangePasswordRequestDTO;
import edu.uniquindio.stayhub_v2.dto.auth.ForgotPasswordRequestDTO;
import edu.uniquindio.stayhub_v2.dto.auth.ResetPasswordRequestDTO;
import edu.uniquindio.stayhub_v2.dto.auth.TokenResponseDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserLoginRequestDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserSignupRequestDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserSignupResponseDTO;
import edu.uniquindio.stayhub_v2.exception.EmailAlreadyExistsException;
import edu.uniquindio.stayhub_v2.exception.InvalidPasswordException;
import edu.uniquindio.stayhub_v2.exception.InvalidRecoveryCodeException;
import edu.uniquindio.stayhub_v2.exception.UserNotFoundException;
import edu.uniquindio.stayhub_v2.mapper.UserMapper;
import edu.uniquindio.stayhub_v2.model.User;
import edu.uniquindio.stayhub_v2.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;

/**
 * Service class for managing user-related operations including authentication,
 * registration, and password recovery.
 *
 * <p>This service handles all user lifecycle operations and serves as the
 * primary facade for user management. It integrates with Spring Security for
 * authentication and password encoding, and with JWT for token generation.</p>
 *
 * <p><b>Core Responsibilities:</b></p>
 * <ul>
 *   <li>User registration with email uniqueness validation</li>
 *   <li>User authentication and JWT token generation</li>
 *   <li>Password recovery flow (forgot/reset password)</li>
 *   <li>Retrieving the current authenticated user</li>
 *   <li>Password encoding and validation</li>
 * </ul>
 *
 * <p><b>Security Features:</b></p>
 * <ul>
 *   <li>Passwords are encoded using BCrypt before storage</li>
 *   <li>Recovery codes expire after 15 minutes</li>
 *   <li>Generic error messages to prevent user enumeration</li>
 *   <li>JWT tokens generated upon successful authentication</li>
 * </ul>
 *
 * <p><b>Password Recovery Flow:</b></p>
 * <ol>
 *   <li>User requests password reset via {@code /forgot-password}</li>
 *   <li>System generates 6-digit code and sends via email</li>
 *   <li>Code expires after 15 minutes</li>
 *   <li>User submits code with new password via {@code /reset-password}</li>
 *   <li>System validates code and updates password</li>
 * </ol>
 *
 * <p><b>Transactional Boundaries:</b></p>
 * <ul>
 *   <li>{@code registerUser}: Write transaction (saves new user)</li>
 *   <li>{@code loginUser}: Read-only transaction (no modifications)</li>
 *   <li>{@code forgotPassword}: Write transaction (updates recovery code)</li>
 *   <li>{@code resetPassword}: Write transaction (updates password)</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * @RestController
 * public class AuthController {
 *
 *     private final UserService userService;
 *
 *     @PostMapping("/register")
 *     public ResponseEntity<UserSignupResponseDTO> register(
 *             @Valid @RequestBody UserSignupRequestDTO request) {
 *         return ResponseEntity.status(HttpStatus.CREATED)
 *                 .body(userService.registerUser(request));
 *     }
 *
 *     @PostMapping("/login")
 *     public ResponseEntity<TokenResponseDTO> login(
 *             @Valid @RequestBody UserLoginRequestDTO request) {
 *         return ResponseEntity.ok(userService.loginUser(request));
 *     }
 * }
 * }</pre>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see UserRepository
 * @see JWTService
 * @see EmailService
 * @see PasswordEncoder
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final EmailService emailService;

    // SecureRandom is cryptographically stronger than Random for security-sensitive codes
    private final Random secureRandom = new SecureRandom();

    /**
     * Retrieves the currently authenticated user from the Spring Security context.
     *
     * <p>This method extracts the authenticated user principal from the
     * {@link SecurityContextHolder}. It is used throughout the application
     * to get the current user for operations that require user context.</p>
     *
     * <p><b>Requirements:</b></p>
     * <ul>
     *   <li>User must be authenticated (not anonymous)</li>
     *   <li>The principal must be a {@link User} entity</li>
     * </ul>
     *
     * <p><b>Usage Example:</b></p>
     * <pre>{@code
     * public void performUserAction() {
     *     User currentUser = userService.getCurrentUser();
     *     log.info("Action performed by: {}", currentUser.getEmail());
     *     // Use currentUser for authorization or data association
     * }
     * }</pre>
     *
     * <p><b>Security Note:</b> This method relies on the {@link JwtAuthenticationFilter}
     * having set the {@link User} entity as the principal in the authentication token.</p>
     *
     * @return The currently authenticated {@link User} entity
     * @throws IllegalStateException if no user is authenticated or principal is anonymous
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || Objects.equals(authentication.getPrincipal(), "anonymousUser")) {
            log.warn("Attempted to get current user but no authenticated user found");
            throw new IllegalStateException("User not authenticated");
        }

        User user = (User) authentication.getPrincipal();
        assert user != null;
        log.debug("Retrieved current user: {}", user.getEmail());
        return user;
    }

    /**
     * Registers a new user in the system.
     *
     * <p>This method handles the complete user registration flow:
     * <ol>
     *   <li>Validates that the email is not already registered</li>
     *   <li>Maps the request DTO to a User entity</li>
     *   <li>Encodes the password using BCrypt</li>
     *   <li>Persists the user to the database</li>
     *   <li>Returns a response DTO with non-sensitive user data</li>
     * </ol>
     *
     * <p><b>Validation:</b></p>
     * <ul>
     *   <li>Email must be unique in the system</li>
     *   <li>Password must meet complexity requirements (handled by DTO validation)</li>
     *   <li>All required fields must be present</li>
     * </ul>
     *
     * <p><b>Default Values:</b></p>
     * <ul>
     *   <li>Role: {@code GUEST} (can be overridden in request)</li>
     *   <li>Deleted: {@code false}</li>
     *   <li>Created/Updated timestamps: Auto-generated</li>
     * </ul>
     *
     * <p><b>Example Request:</b></p>
     * <pre>{@code
     * {
     *   "fullName": "John Doe",
     *   "email": "john@example.com",
     *   "password": "SecurePass123",
     *   "phoneNumber": "+573001234567",
     *   "birthDate": "1990-01-01"
     * }
     * }</pre>
     *
     * <p><b>Example Response:</b></p>
     * <pre>{@code
     * {
     *   "id": 123,
     *   "fullName": "John Doe",
     *   "email": "john@example.com",
     *   "role": "GUEST"
     * }
     * }</pre>
     *
     * @param userSignupRequestDTO The registration request containing user data
     * @return UserSignupResponseDTO containing the created user's non-sensitive data
     * @throws EmailAlreadyExistsException if the email is already registered
     */
    @Transactional
    public UserSignupResponseDTO registerUser(@Valid UserSignupRequestDTO userSignupRequestDTO) {
        log.info("Processing registration request for email: {}", userSignupRequestDTO.email());

        // Validate email uniqueness
        if (userRepository.findByEmail(userSignupRequestDTO.email()).isPresent()) {
            log.warn("Registration failed: Email already exists - {}", userSignupRequestDTO.email());
            throw new EmailAlreadyExistsException("Email already exists");
        }

        // Map DTO to entity
        User user = userMapper.toEntity(userSignupRequestDTO);
        log.debug("User mapped to entity successfully: {}", user.getEmail());

        // Encode password
        user.setPassword(passwordEncoder.encode(userSignupRequestDTO.password()));
        log.debug("Password encoded for user: {}", user.getEmail());

        // Persist user
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {} (ID: {})", savedUser.getEmail(), savedUser.getId());

        return userMapper.toSignupResponseDTO(savedUser);
    }

    /**
     * Authenticates a user and generates a JWT token.
     *
     * <p>This method validates user credentials and returns a JWT token
     * that can be used for authenticating later requests. The token
     * contains the user's email, ID, and roles as claims.</p>
     *
     * <p><b>Authentication Flow:</b></p>
     * <ol>
     *   <li>Find the user by email</li>
     *   <li>Verify password using BCrypt</li>
     *   <li>Generate JWT token with user claims</li>
     *   <li>Return token in response DTO</li>
     * </ol>
     *
     * <p><b>Security Considerations:</b></p>
     * <ul>
     *   <li>Generic error message prevents user enumeration</li>
     *   <li>Failed attempts are logged for monitoring</li>
     *   <li>Password is never logged or returned in response</li>
     *   <li>Token expiration is configured via {@code JWT.TIME.EXPIRATION}</li>
     * </ul>
     *
     * <p><b>Example Request:</b></p>
     * <pre>{@code
     * {
     *   "email": "john@example.com",
     *   "password": "SecurePass123"
     * }
     * }</pre>
     *
     * <p><b>Example Response:</b></p>
     * <pre>{@code
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * }
     * }</pre>
     *
     * <p><b>Read-Only Transaction:</b> This method is marked as read-only
     * since it only performs read operations and token generation.</p>
     *
     * @param userLoginRequestDTO The login request containing email and password
     * @return TokenResponseDTO containing the JWT token
     * @throws UserNotFoundException if no user exists with the given email
     * @throws InvalidPasswordException if the password does not match
     */
    @Transactional(readOnly = true)
    public TokenResponseDTO loginUser(@Valid UserLoginRequestDTO userLoginRequestDTO) {
        log.info("Processing login request for email: {}", userLoginRequestDTO.email());

        // Find the user by email
        User user = userRepository.findByEmail(userLoginRequestDTO.email())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found with email: {}", userLoginRequestDTO.email());
                    return new UserNotFoundException("Invalid credentials");
                });
        log.debug("User found with email: {}", user.getEmail());

        // Verify password
        if (!passwordEncoder.matches(userLoginRequestDTO.password(), user.getPassword())) {
            log.warn("Login failed: Invalid password for email: {}", userLoginRequestDTO.email());
            throw new InvalidPasswordException("Invalid credentials");
        }

        // Generate JWT token
        String token = jwtService.generateToken(user);
        log.info("User logged in successfully: {}", user.getEmail());

        return new TokenResponseDTO(token);
    }

    /**
     * Initiates the password recovery process by sending a recovery code via email.
     *
     * <p>This method generates a 6-digit recovery code, associates it with the user,
     * sets a 15-minute expiration, and sends the code to the user's email address.</p>
     *
     * <p><b>Recovery Code Characteristics:</b></p>
     * <ul>
     *   <li>6-digit numeric code (100000-999999)</li>
     *   <li>Expires after 15 minutes</li>
     *   <li>Single-use (cleared after successful password reset)</li>
     *   <li>Stored in plain text (consider hashing in production)</li>
     * </ul>
     *
     * <p><b>Security Considerations:</b></p>
     * <ul>
     *   <li>Generic response prevents user enumeration (doesn't reveal if email exists)</li>
     *   <li>Codes are generated using cryptographically secure random</li>
     *   <li>Rate limiting should be implemented at the controller/network level</li>
     *   <li>Consider using email templates instead of plain text for better UX</li>
     * </ul>
     *
     * <p><b>Example Request:</b></p>
     * <pre>{@code
     * {
     *   "email": "john@example.com"
     * }
     * }</pre>
     *
     * <p><b>Email Content Example:</b></p>
     * <pre>
     * Hola John Doe,
     *
     * Has solicitado recuperar tu contraseña. Usa el siguiente código para reestablecerla:
     *
     * Código: 123456
     *
     * Este código es válido por 15 minutos.
     * </pre>
     *
     * @param requestDTO The forgot password request containing user email
     * @throws UserNotFoundException if no user exists with the given email
     */
    @Transactional
    public void forgotPassword(@Valid ForgotPasswordRequestDTO requestDTO) {
        log.info("Processing forgot password request for email: {}", requestDTO.email());

        // Find the user by email
        User user = userRepository.findByEmail(requestDTO.email())
                .orElseThrow(() -> {
                    log.warn("Forgot password failed: User not found with email: {}", requestDTO.email());
                    return new UserNotFoundException("User not found");
                });

        // Generate and save recovery code
        String code = generateRecoveryCode();
        user.setPasswordRecoveryCode(code);
        user.setPasswordRecoveryExpiration(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        log.debug("Recovery code generated and saved for user: {}", user.getEmail());

        // Build and send email
        String text = buildRecoveryEmailText(user.getFullName(), code);
        emailService.sendEmail(user.getEmail(), "Recuperación de contraseña - StayHub", text);

        log.info("Recovery code sent to email: {}", user.getEmail());
    }

    /**
     * Resets the user's password using a valid recovery code.
     *
     * <p>This method validates the recovery code and its expiration,
     * then updates the user's password with a new encoded password.
     * The recovery code is cleared after a successful password reset.</p>
     *
     * <p><b>Validation Steps:</b></p>
     * <ol>
     *   <li>Verify the user exists with the given email</li>
     *   <li>Check that recovery code matches the stored code</li>
     *   <li>Verify the code has not expired (valid for 15 minutes)</li>
     *   <li>Encode and save the new password</li>
     *   <li>Clear the recovery code and expiration</li>
     * </ol>
     *
     * <p><b>Example Request:</b></p>
     * <pre>{@code
     * {
     *   "email": "john@example.com",
     *   "code": "123456",
     *   "newPassword": "NewSecurePass456"
     * }
     * }</pre>
     *
     * <p><b>Security Considerations:</b></p>
     * <ul>
     *   <li>Recovery codes are single-use</li>
     *   <li>Codes expire after 15 minutes</li>
     *   <li>A new password must meet complexity requirements</li>
     *   <li>Consider invalidating all existing sessions/tokens after a password change</li>
     * </ul>
     *
     * @param requestDTO The reset password request containing email, code, and a new password
     * @throws UserNotFoundException if no user exists with the given email
     * @throws InvalidRecoveryCodeException if the code is invalid or expired
     */
    @Transactional
    public void resetPassword(@Valid ResetPasswordRequestDTO requestDTO) {
        log.info("Processing reset password request for email: {}", requestDTO.email());

        // Find the user by email
        User user = userRepository.findByEmail(requestDTO.email())
                .orElseThrow(() -> {
                    log.warn("Reset password failed: User not found with email: {}", requestDTO.email());
                    return new UserNotFoundException("User not found");
                });

        // Validate recovery code
        if (user.getPasswordRecoveryCode() == null ||
                !user.getPasswordRecoveryCode().equals(requestDTO.code())) {
            log.warn("Reset password failed: Invalid recovery code for email: {}", requestDTO.email());
            throw new InvalidRecoveryCodeException("El código de recuperación es inválido");
        }

        // Validate code expiration
        if (user.getPasswordRecoveryExpiration() == null ||
                user.getPasswordRecoveryExpiration().isBefore(LocalDateTime.now())) {
            log.warn("Reset password failed: Expired recovery code for email: {}", requestDTO.email());
            throw new InvalidRecoveryCodeException("El código de recuperación ha expirado");
        }

        // Update password and clear recovery data
        user.setPassword(passwordEncoder.encode(requestDTO.newPassword()));
        user.setPasswordRecoveryCode(null);
        user.setPasswordRecoveryExpiration(null);

        userRepository.save(user);
        log.info("Password successfully reset for user: {}", user.getEmail());
    }

    /**
     * Generates a cryptographically secure 6-digit recovery code.
     *
     * <p>Uses {@link SecureRandom} to generate unpredictable codes
     * between 100,000 and 999,999.</p>
     *
     * @return A 6-digit numeric code as a string
     */
    private String generateRecoveryCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Builds the plain text content for the password recovery email.
     *
     * @param fullName The recipient's full name
     * @param code The recovery code
     * @return Formatted email text
     */
    private String buildRecoveryEmailText(String fullName, String code) {
        return String.format("""
                Hola %s,
               \s
                Has solicitado recuperar tu contraseña en StayHub.\s
                Usa el siguiente código para reestablecerla:
               \s
                Código: %s
               \s
                Este código es válido por 15 minutos.
               \s
                Si no solicitaste este cambio, puedes ignorar este mensaje.
               \s
                Saludos,
                El equipo de StayHub 🏡
               \s""", fullName, code);
    }

    @Transactional
    public void changePassword(String email, @Valid ChangePasswordRequestDTO requestDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(requestDTO.currentPassword(), user.getPassword())) {
            log.warn("Invalid current password attempt for changing password");
            throw new InvalidPasswordException("La contraseña actual es incorrecta");
        }

        user.setPassword(passwordEncoder.encode(requestDTO.newPassword()));
        userRepository.save(user);
        log.info("Password successfully changed for {}", user.getEmail());
    }

    /*
     * Additional methods that could be added in the future:
     *
     * // Update user profile
     * @Transactional
     * public UserProfileResponseDTO updateProfile(Long userId, UpdateProfileRequestDTO request) {
     *     User user = userRepository.findById(userId)
     *             .orElseThrow(() -> new UserNotFoundException("User not found"));
     *
     *     // Validate current user can only update their own profile
     *     validateOwnership(user, getCurrentUser());
     *
     *     // Update allowed fields
     *     user.setFullName(request.fullName());
     *     user.setPhoneNumber(request.phoneNumber());
     *     user.setProfilePicture(request.profilePicture());
     *
     *     return userMapper.toProfileResponseDTO(userRepository.save(user));
     * }
     *
     * // Change password (authenticated user)
     * @Transactional
     * public void changePassword(String currentPassword, String newPassword) {
     *     User user = getCurrentUser();
     *
     *     if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
     *         throw new InvalidPasswordException("Current password is incorrect");
     *     }
     *
     *     user.setPassword(passwordEncoder.encode(newPassword));
     *     userRepository.save(user);
     *
     *     // Invalidate all existing tokens/sessions
     * }
     *
     * // Soft delete user account
     * @Transactional
     * public void deactivateAccount(String password) {
     *     User user = getCurrentUser();
     *
     *     if (!passwordEncoder.matches(password, user.getPassword())) {
     *         throw new InvalidPasswordException("Invalid password");
     *     }
     *
     *     user.setDeleted(true);
     *     userRepository.save(user);
     * }
     *
     * // Verify email (with verification token)
     * @Transactional
     * public void verifyEmail(String token) {
     *     // Implementation
     * }
     *
     * // Resend verification email
     * public void resendVerificationEmail(String email) {
     *     // Implementation
     * }
     */
}
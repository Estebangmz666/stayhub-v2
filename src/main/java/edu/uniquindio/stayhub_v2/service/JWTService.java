package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.model.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Service class for JWT (JSON Web Token) operations including generation,
 * validation, and extraction of claims.
 *
 * <p>This service handles all JWT-related operations for the StayHub platform,
 * providing stateless authentication through signed tokens. It uses the
 * HMAC-SHA algorithm for token signing and includes user information as claims.</p>
 *
 * <p><b>Token Structure:</b></p>
 * <ul>
 *   <li><b>Header:</b> Algorithm (HS256) and token type (JWT)</li>
 *   <li><b>Payload (Claims):</b>
 *     <ul>
 *       <li>{@code sub} - User email (subject)</li>
 *       <li>{@code userId} - User's unique identifier</li>
 *       <li>{@code roles} - List of user roles (GUEST, HOST)</li>
 *       <li>{@code iat} - Issued at timestamp</li>
 *       <li>{@code exp} - Expiration timestamp</li>
 *     </ul>
 *   </li>
 *   <li><b>Signature:</b> HMAC-SHA256 of header and payload</li>
 * </ul>
 *
 * <p><b>Configuration Required:</b></p>
 * The following properties must be defined in {@code application.yml}:
 * <pre>{@code
 * JWT:
 *   SECRET:
 *     KEY: your-256-bit-secret-key-here-minimum-32-characters
 *   TIME:
 *     EXPIRATION: 86400000  # 24 hours in milliseconds
 * }</pre>
 *
 * <p><b>Security Considerations:</b></p>
 * <ul>
 *   <li>The secret key must be at least 256 bits (32 characters) for HS256</li>
 *   <li>Never expose the secret key in logs or error messages</li>
 *   <li>Store the secret key securely (environment variables, Vault, etc.)</li>
 *   <li>Tokens should be transmitted only over HTTPS</li>
 *   <li>Consider implementing token revocation for critical operations</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * @Service
 * public class AuthService {
 *
 *     private final JWTService jwtService;
 *
 *     public String authenticate(String email, String password) {
 *         User user = userRepository.findByEmail(email)
 *                 .orElseThrow(() -> new UserNotFoundException("User not found"));
 *
 *         if (passwordEncoder.matches(password, user.getPassword())) {
 *             return jwtService.generateToken(user);
 *         }
 *         throw new InvalidPasswordException("Invalid credentials");
 *     }
 * }
 * }</pre>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see io.jsonwebtoken.Jwts
 * @see User
 */
@Service
@Slf4j
public class JWTService {

    private final SecretKey SECRET_KEY;
    private final long EXPIRATION_TIME;

    /**
     * Constructs a JWTService with the configured secret key and expiration time.
     *
     * <p>The secret key is used for signing and verifying JWT tokens using the
     * HMAC-SHA256 algorithm. The expiration time defines how long generated
     * tokens remain valid.</p>
     *
     * <p><b>Secret Key Requirements:</b></p>
     * <ul>
     *   <li>Minimum 256 bits (32 characters) for HS256 algorithm</li>
     *   <li>Should be cryptographically random</li>
     *   <li>Must be kept confidential and never exposed</li>
     * </ul>
     *
     * <p><b>Example Secret Key Generation:</b></p>
     * <pre>{@code
     * // Generate a secure 256-bit key (for reference only)
     * SecretKey key = Jwts.SIG.HS256.key().build();
     * String encoded = Encoders.BASE64.encode(key.getEncoded());
     * }</pre>
     *
     * @param secretKey The secret key for signing JWT tokens, loaded from {@code JWT.SECRET.KEY}
     * @param expirationTime The token expiration time in milliseconds, loaded from {@code JWT.TIME.EXPIRATION}
     */
    public JWTService(
            @Value("${JWT.SECRET.KEY}") String secretKey,
            @Value("${JWT.TIME.EXPIRATION}") long expirationTime) {

        this.SECRET_KEY = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.EXPIRATION_TIME = expirationTime;

        log.debug("JWTService initialized with expiration time: {} ms", expirationTime);
    }

    /**
     * Generates a JWT token for an authenticated user.
     *
     * <p>Creates a signed JWT token containing the user's email as the subject
     * and additional claims including user ID and roles. The token is signed
     * using HMAC-SHA256 and includes issuance and expiration timestamps.</p>
     *
     * <p><b>Token Claims:</b></p>
     * <table border="1">
     *   <tr><th>Claim</th><th>Description</th><th>Example</th></tr>
     *   <tr><td>{@code sub}</td><td>User email (subject)</td><td>user@example.com</td></tr>
     *   <tr><td>{@code userId}</td><td>User's unique ID</td><td>12345</td></tr>
     *   <tr><td>{@code roles}</td><td>List of role names</td><td>["GUEST", "HOST"]</td></tr>
     *   <tr><td>{@code iat}</td><td>Issued at timestamp</td><td>1700000000</td></tr>
     *   <tr><td>{@code exp}</td><td>Expiration timestamp</td><td>1700086400</td></tr>
     * </table>
     *
     * <p><b>Example Generated Token (decoded payload):</b></p>
     * <pre>{@code
     * {
     *   "sub": "john@example.com",
     *   "userId": 123,
     *   "roles": ["GUEST"],
     *   "iat": 1700000000,
     *   "exp": 1700086400
     * }
     * }</pre>
     *
     * @param user The authenticated user for whom to generate the token
     * @return A compact JWT token string (format: {@code header.payload.signature})
     * @throws IllegalArgumentException if user is null or missing required fields
     */
    public String generateToken(User user) {
        log.info("Generating JWT token for user: {}", user.getEmail());

        String token = Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim(
                        "roles",
                        user.getRoles()
                                .stream()
                                .map(Enum::name)
                                .toList()
                )
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();

        log.debug("JWT token generated successfully for user: {}", user.getEmail());
        return token;
    }

    /**
     * Validates a JWT token by verifying its signature and expiration.
     *
     * <p>This method performs cryptographic verification of the token's signature
     * using the configured secret key and checks that the token has not expired.
     * It does NOT validate the claims or check if the user still exists.</p>
     *
     * <p><b>Validation Steps:</b></p>
     * <ol>
     *   <li>Parse the token and verify signature using the secret key</li>
     *   <li>Check that the token has not expired ({@code exp} claim)</li>
     *   <li>Verify the token structure is valid</li>
     * </ol>
     *
     * <p><b>Usage Pattern:</b></p>
     * <pre>{@code
     * public boolean authenticateRequest(String authHeader) {
     *     if (authHeader == null || !authHeader.startsWith("Bearer ")) {
     *         return false;
     *     }
     *
     *     String token = authHeader.substring(7);
     *     try {
     *         return jwtService.validateToken(token);
     *     } catch (ExpiredJwtException e) {
     *         log.warn("Token expired for request");
     *         return false;
     *     } catch (JwtException e) {
     *         log.warn("Invalid token: {}", e.getMessage());
     *         return false;
     *     }
     * }
     * }</pre>
     *
     * @param token The JWT token string to validate
     * @return {@code true} if the token is valid (properly signed and not expired)
     * @throws ExpiredJwtException If the token has expired (exp claim is in the past)
     * @throws JwtException If the token is invalid (malformed, incorrect signature, or unsupported)
     */
    public boolean validateToken(String token) {
        log.debug("Validating JWT token");

        try {
            Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token);

            log.debug("JWT token validated successfully");
            return true;

        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            throw e;

        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extracts the user's email (subject) from a JWT token.
     *
     * <p>This method parses the token, verifies its signature, and extracts
     * the subject claim which contains the user's email address. The token
     * must be valid and properly signed.</p>
     *
     * <p><b>Return Value:</b></p>
     * <ul>
     *   <li>The email address (subject) if the token is valid</li>
     *   <li>{@code null} if the token is invalid, expired, or malformed</li>
     * </ul>
     *
     * <p><b>⚠️ Note:</b> This method returns {@code null} for invalid tokens
     * rather than throwing exceptions. This design choice prevents exception
     * propagation in filter chains but may hide the specific reason for failure.
     * Consider using {@link #validateToken(String)} first if you need detailed
     * error information.</p>
     *
     * <p><b>Usage in JWT Filter:</b></p>
     * <pre>{@code
     * String token = authHeader.substring(7);
     * String email = jwtService.getEmailFromToken(token);
     *
     * if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
     *     User user = userRepository.findByEmail(email).orElse(null);
     *     if (user != null) {
     *         // Set authentication in context
     *     }
     * }
     * }</pre>
     *
     * @param token The JWT token to extract the email from
     * @return The user's email address (subject claim), or {@code null} if the token is invalid
     */
    public String getEmailFromToken(String token) {
        log.debug("Extracting email from JWT token");

        try {
            String email = Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();

            log.debug("Successfully extracted email from token: {}", email);
            return email;

        } catch (JwtException e) {
            log.warn("Failed to extract email from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts a specific claim from a JWT token.
     *
     * <p>Utility method to retrieve any claim from the token payload.</p>
     *
     * @param token The JWT token
     * @param claimName The name of the claim to extract
     * @return The claim value, or {@code null} if not present or token invalid
     */
    /*
    public Object getClaimFromToken(String token, String claimName) {
        try {
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get(claimName);
        } catch (JwtException e) {
            log.warn("Failed to extract claim '{}' from token: {}", claimName, e.getMessage());
            return null;
        }
    }
    */

    /*
     * Additional methods that could be added in the future:
     *
     * // Extract user ID from token
     * public Long getUserIdFromToken(String token) {
     *     try {
     *         return Jwts.parser()
     *                 .verifyWith(SECRET_KEY)
     *                 .build()
     *                 .parseSignedClaims(token)
     *                 .getPayload()
     *                 .get("userId", Long.class);
     *     } catch (JwtException e) {
     *         log.warn("Failed to extract user ID from token: {}", e.getMessage());
     *         return null;
     *     }
     * }
     *
     * // Extract roles from token
     * public List<String> getRolesFromToken(String token) {
     *     try {
     *         return Jwts.parser()
     *                 .verifyWith(SECRET_KEY)
     *                 .build()
     *                 .parseSignedClaims(token)
     *                 .getPayload()
     *                 .get("roles", List.class);
     *     } catch (JwtException e) {
     *         log.warn("Failed to extract roles from token: {}", e.getMessage());
     *         return Collections.emptyList();
     *     }
     * }
     *
     * // Generate refresh token (longer expiration)
     * public String generateRefreshToken(User user) {
     *     // Implementation with longer expiration time
     * }
     *
     * // Invalidate token (requires token blacklist/Redis)
     * public void invalidateToken(String token) {
     *     // Add token to blacklist with expiration
     * }
     *
     * // Check if token is blacklisted
     * public boolean isTokenBlacklisted(String token) {
     *     // Check against Redis/DB blacklist
     * }
     *
     * // Get remaining validity time
     * public long getTokenRemainingValidity(String token) {
     *     try {
     *         Date expiration = Jwts.parser()
     *                 .verifyWith(SECRET_KEY)
     *                 .build()
     *                 .parseSignedClaims(token)
     *                 .getPayload()
     *                 .getExpiration();
     *         return expiration.getTime() - System.currentTimeMillis();
     *     } catch (JwtException e) {
     *         return 0;
     *     }
     * }
     */
}
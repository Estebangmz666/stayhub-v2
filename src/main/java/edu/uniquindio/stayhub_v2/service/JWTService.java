package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.model.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Service class for JWT-related operations.
 */
@Service @Slf4j
public class JWTService {

    private final SecretKey SECRET_KEY;
    private final long EXPIRATION_TIME;

    /**
     * Constructs a JwtService with the secret key and expiration time loaded from environment variables.
     *
     * @param secretKey The secret key for signing JWT tokens, loaded from application properties.
     * @param expirationTime The token expiration time in milliseconds, loaded from application properties.
     */
    public JWTService(@Value("${JWT.SECRET.KEY}") String secretKey, @Value("${JWT.TIME.EXPIRATION}") long expirationTime) {
        this.SECRET_KEY = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.EXPIRATION_TIME = expirationTime;
    }

    /**
     * Generates a JWT token for a user, including their email as the subject and a role as a claim.
     *
     * @param user The user for whom the token is generated.
     * @return The generated JWT token as a string.
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
        log.debug("JWT token generated for user: {}", user.getEmail());
        return token;
    }

    /**
     * Validates a JWT token by checking its signature and expiration.
     *
     * @param token The JWT token to validate.
     * @return True if the token is valid, false otherwise.
     * @throws ExpiredJwtException If the token has expired.
     * @throws JwtException If the token is invalid (e.g., malformed or incorrect signature).
     */
    public boolean validateToken(String token) {
        log.info("Validating JWT token");
        try {
            Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token);
            log.debug("JWT token validated successfully");
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT token expired: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        }
    }
}
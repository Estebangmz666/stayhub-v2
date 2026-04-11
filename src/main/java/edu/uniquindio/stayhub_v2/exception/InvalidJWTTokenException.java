// InvalidJWTTokenException.java (NOMBRE CORREGIDO - sin typo y sin duplicado)
package edu.uniquindio.stayhub_v2.exception;

/**
 * Exception thrown when a JWT token is invalid, expired, or malformed.
 *
 * <p>This exception is typically thrown during token validation when:
 * <ul>
 *   <li>The token signature is invalid or tampered with</li>
 *   <li>The token has expired</li>
 *   <li>The token is malformed or cannot be parsed</li>
 *   <li>The token issuer is not trusted</li>
 *   <li>The token has been revoked</li>
 * </ul>
 *
 * <p>This exception results in a {@code 401 Unauthorized} HTTP response
 * when thrown from a controller endpoint, indicating that the client
 * must obtain a new valid token.</p>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 */
public class InvalidJWTTokenException extends RuntimeException {

    /**
     * Constructs a new InvalidJWTTokenException with the specified detail message.
     *
     * @param message The detail message explaining the reason for the exception
     */
    public InvalidJWTTokenException(String message) {
        super(message);
    }
}
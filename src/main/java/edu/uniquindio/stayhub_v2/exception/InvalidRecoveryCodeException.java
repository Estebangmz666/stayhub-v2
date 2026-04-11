package edu.uniquindio.stayhub_v2.exception;

/**
 * Exception thrown when a password recovery code is invalid.
 *
 * <p>This exception is typically thrown when:
 * <ul>
 *   <li>The recovery code has expired</li>
 *   <li>The recovery code has already been used</li>
 *   <li>The recovery code does not exist in the system</li>
 *   <li>The recovery code is associated with a different user</li>
 * </ul>
 *
 * <p>This exception results in a {@code 400 Bad Request} HTTP response
 * when thrown from a controller endpoint.</p>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 */
public class InvalidRecoveryCodeException extends RuntimeException {

    /**
     * Constructs a new InvalidRecoveryCodeException with the specified detail message.
     *
     * @param message The detail message explaining the reason for the exception
     */
    public InvalidRecoveryCodeException(String message) {
        super(message);
    }
}
package edu.uniquindio.stayhub_v2.exception;

/**
 * Exception thrown when a user provides an incorrect password.
 *
 * <p>This exception is typically thrown during:
 * <ul>
 *   <li>Login attempts with invalid credentials</li>
 *   <li>Password change operations where the current password is incorrect</li>
 *   <li>Account verification processes</li>
 * </ul>
 *
 * <p>For security reasons, the error message returned to the client
 * should be generic (e.g., "Invalid credentials") and not reveal whether
 * the email or password was incorrect.</p>
 *
 * <p>This exception results in a {@code 401 Unauthorized} HTTP response
 * when thrown from a controller endpoint.</p>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 */
public class InvalidPasswordException extends RuntimeException {

    /**
     * Constructs a new InvalidPasswordException with the specified detail message.
     *
     * @param message The detail message explaining the reason for the exception
     */
    public InvalidPasswordException(String message) {
        super(message);
    }
}
package edu.uniquindio.stayhub_v2.exception;

/**
 * Exception thrown when attempting to register with an email that already exists.
 *
 * <p>This exception is typically thrown during user registration when:
 * <ul>
 *   <li>A user attempts to create an account with an email already in use</li>
 *   <li>Updating a user profile with an email belonging to another user</li>
 * </ul>
 *
 * <p>This exception results in a {@code 409 Conflict} or {@code 400 Bad Request}
 * HTTP response when thrown from a controller endpoint.</p>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 */
public class EmailAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new EmailAlreadyExistsException with the specified detail message.
     *
     * @param message The detail message explaining the reason for the exception
     */
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
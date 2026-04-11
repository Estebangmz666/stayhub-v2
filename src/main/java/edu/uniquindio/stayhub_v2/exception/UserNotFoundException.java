package edu.uniquindio.stayhub_v2.exception;

/**
 * Exception thrown when a user cannot be found in the system.
 *
 * <p>This exception is typically thrown when:
 * <ul>
 *   <li>Attempting to retrieve a user by an email that doesn't exist</li>
 *   <li>Looking up a user by an ID that doesn't exist</li>
 *   <li>A referenced user in a relationship no longer exists</li>
 * </ul>
 *
 * <p>For security reasons, during authentication flows, this exception
 * should be caught and transformed into a generic "Invalid credentials"
 * message to prevent user enumeration attacks.</p>
 *
 * <p>This exception results in a {@code 404 Not Found} or
 * {@code 401 Unauthorized} HTTP response depending on the context.</p>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Constructs a new UserNotFoundException with the specified detail message.
     *
     * @param message The detail message explaining the reason for the exception
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
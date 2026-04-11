package edu.uniquindio.stayhub_v2.exception;

/**
 * Exception thrown when a user attempts to perform an action on a resource
 * they do not own.
 *
 * <p>This exception is typically thrown when:
 * <ul>
 *   <li>A host tries to modify or delete another host's accommodation</li>
 *   <li>A user attempts to access reservation details belonging to another user</li>
 *   <li>A host tries to view booking information for properties they don't own</li>
 * </ul>
 *
 * <p>This exception enforces the security boundary between different users'
 * resources and ensures data isolation.</p>
 *
 * <p>This exception results in a {@code 403 Forbidden} HTTP response
 * when thrown from a controller endpoint.</p>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 */
public class UnauthorizedHostException extends RuntimeException {

    /**
     * Constructs a new UnauthorizedHostException with the specified detail message.
     *
     * @param message The detail message explaining the reason for the exception
     */
    public UnauthorizedHostException(String message) {
        super(message);
    }
}
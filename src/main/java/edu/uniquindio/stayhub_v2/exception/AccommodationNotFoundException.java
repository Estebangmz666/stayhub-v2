package edu.uniquindio.stayhub_v2.exception;

/**
 * Exception thrown when an accommodation cannot be found in the system.
 *
 * <p>This exception is typically thrown when:
 * <ul>
 *   <li>Attempting to retrieve an accommodation by an ID that doesn't exist</li>
 *   <li>Searching for an accommodation that has been deleted</li>
 *   <li>Accessing an accommodation with an invalid identifier</li>
 * </ul>
 *
 * <p>This exception results in a {@code 404 Not Found} HTTP response
 * when thrown from a controller endpoint.</p>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 */
public class AccommodationNotFoundException extends RuntimeException {

    /**
     * Constructs a new AccommodationNotFoundException with the specified detail message.
     *
     * @param message The detail message explaining the reason for the exception
     */
    public AccommodationNotFoundException(String message) {
        super(message);
    }
}
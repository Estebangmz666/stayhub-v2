package edu.uniquindio.stayhub_v2.exception;

/**
 * Exception thrown when an operation cannot be completed due to active reservations.
 *
 * <p>This exception is typically thrown when:
 * <ul>
 *   <li>Attempting to delete an accommodation that has future or ongoing reservations</li>
 *   <li>Modifying an accommodation that is currently booked</li>
 *   <li>Performing an action that would disrupt existing guest stays</li>
 * </ul>
 *
 * <p>This exception protects guests with existing bookings from disruption
 * and ensures hosts cannot cancel or modify properties with active stays.</p>
 *
 * <p>This exception results in a {@code 400 Bad Request} HTTP response
 * when thrown from a controller endpoint.</p>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 */
public class ActiveReservationsException extends RuntimeException {

    /**
     * Constructs a new ActiveReservationsException with the specified detail message.
     *
     * @param message The detail message explaining the reason for the exception
     */
    public ActiveReservationsException(String message) {
        super(message);
    }
}
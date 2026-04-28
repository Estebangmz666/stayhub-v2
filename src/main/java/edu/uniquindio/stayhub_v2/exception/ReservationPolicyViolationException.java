package edu.uniquindio.stayhub_v2.exception;

/**
 * Exception thrown when a reservation operation violates timing or lifecycle
 * policies.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
public class ReservationPolicyViolationException extends RuntimeException {

    public ReservationPolicyViolationException(String message) {
        super(message);
    }
}

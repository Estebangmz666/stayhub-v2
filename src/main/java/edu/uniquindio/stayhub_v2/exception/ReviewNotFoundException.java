package edu.uniquindio.stayhub_v2.exception;

/**
 * Exception thrown when a requested review cannot be found.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException(String message) {
        super(message);
    }
}

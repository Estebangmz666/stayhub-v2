package edu.uniquindio.stayhub_v2.exception;

/**
 * Exception thrown when a review operation violates a business rule.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
public class ReviewPolicyViolationException extends RuntimeException {

    public ReviewPolicyViolationException(String message) {
        super(message);
    }
}

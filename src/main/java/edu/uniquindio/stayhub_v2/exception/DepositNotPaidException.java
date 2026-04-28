package edu.uniquindio.stayhub_v2.exception;

/**
 * Exception thrown when a reservation operation requires the deposit to be
 * paid first.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
public class DepositNotPaidException extends RuntimeException {

    public DepositNotPaidException(String message) {
        super(message);
    }
}

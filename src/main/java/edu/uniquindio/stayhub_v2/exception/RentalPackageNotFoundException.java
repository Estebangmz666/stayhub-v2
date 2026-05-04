package edu.uniquindio.stayhub_v2.exception;

/**
 * Exception thrown when a requested rental package cannot be found in the database.
 *
 * <p>This exception is typically raised by {@code RentalPackageService} when a package lookup
 * by ID returns no result. It is handled globally by {@code GlobalExceptionHandler},
 * which maps it to an HTTP 404 NOT FOUND response.</p>
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
public class RentalPackageNotFoundException extends RuntimeException {

    /**
     * Constructs a new exception with the given detail message.
     *
     * @param message human-readable description of the missing package
     */
    public RentalPackageNotFoundException(String message) {
        super(message);
    }
}

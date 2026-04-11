package edu.uniquindio.stayhub_v2.exception;

/**
 * Exception thrown when an email notification fails to be processed.
 *
 * <p>This exception is used as a wrapper for various email-related failures,
 * such as template processing errors, invalid email addresses, or
 * context building failures.</p>
 *
 * <p><b>Note:</b> This is a runtime exception to avoid forcing explicit
 * handling in async event listeners and to prevent email failures from
 * affecting the main business flow.</p>
 *
 * <p>Typical scenarios where this exception is thrown:
 * <ul>
 *   <li>Building the Thymeleaf context fails</li>
 *   <li>Email template is missing or corrupted</li>
 *   <li>Recipient email address is invalid</li>
 * </ul>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 */
public class EmailNotificationException extends RuntimeException {

    /**
     * Constructs a new EmailNotificationException with the specified detail message.
     *
     * @param message The detail message explaining the reason for the exception
     */
    public EmailNotificationException(String message) {
        super(message);
    }

    /**
     * Constructs a new EmailNotificationException with the specified detail message
     * and cause.
     *
     * @param message The detail message explaining the reason for the exception
     * @param cause The underlying cause of this exception
     */
    public EmailNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
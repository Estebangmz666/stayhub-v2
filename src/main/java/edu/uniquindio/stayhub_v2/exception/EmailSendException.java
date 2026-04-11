package edu.uniquindio.stayhub_v2.exception;

/**
 * Exception thrown when the email service fails to send an email.
 *
 * <p>This exception wraps lower-level email sending failures such as:
 * <ul>
 *   <li>SMTP server connection issues</li>
 *   <li>Authentication failures with the email provider</li>
 *   <li>Network timeouts</li>
 *   <li>Rate limiting from the email service</li>
 * </ul>
 *
 * <p>Unlike {@link EmailNotificationException}, this exception specifically
 * indicates a failure in the actual transmission of the email, not in the
 * preparation or templating phase.</p>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see EmailNotificationException
 */
public class EmailSendException extends RuntimeException {

    /**
     * Constructs a new EmailSendException with the specified detail message
     * and cause.
     *
     * @param message The detail message explaining the reason for the exception
     * @param cause The underlying cause of this exception
     */
    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
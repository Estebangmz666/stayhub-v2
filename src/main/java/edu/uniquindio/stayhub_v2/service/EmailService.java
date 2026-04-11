package edu.uniquindio.stayhub_v2.service;

import org.thymeleaf.context.Context;

/**
 * Service interface for sending emails in the StayHub platform.
 *
 * <p>This interface defines the contract for email operations, supporting both
 * plain text emails and HTML emails rendered from Thymeleaf templates.
 * Implementations of this interface handle the actual email delivery mechanism
 * (e.g., SMTP, external email API).</p>
 *
 * <p><b>Email Types Supported:</b></p>
 * <ul>
 *   <li><b>Plain Text:</b> Simple text-only emails for basic notifications</li>
 *   <li><b>HTML Templates:</b> Rich, styled emails using Thymeleaf templates</li>
 * </ul>
 *
 * <p><b>Template Naming Convention:</b></p>
 * Templates should be placed in {@code src/main/resources/templates/} with
 * descriptive names. The {@code templateName} parameter should be specified
 * without the {@code .html} extension.
 *
 * <p><b>Example Templates:</b></p>
 * <ul>
 *   <li>{@code reservation-confirmation} - Sent to guests upon booking</li>
 *   <li>{@code host-reservation-notification} - Sent to hosts for new bookings</li>
 *   <li>{@code password-recovery} - Sent for password reset requests</li>
 *   <li>{@code welcome} - Welcome email for new users</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * @Service
 * public class NotificationService {
 *
 *     private final EmailService emailService;
 *
 *     public void sendBookingConfirmation(User guest, Reservation reservation) {
 *         Context context = new Context();
 *         context.setVariable("userName", guest.getFullName());
 *         context.setVariable("accommodationTitle", reservation.getAccommodation().getTitle());
 *         context.setVariable("startDate", reservation.getStartDate());
 *         context.setVariable("endDate", reservation.getEndDate());
 *         context.setVariable("totalPrice", reservation.getTotalPrice());
 *
 *         emailService.sendEmailWithTemplate(
 *                 guest.getEmail(),
 *                 "✨ Tu reserva está confirmada",
 *                 "reservation-confirmation",
 *                 context
 *         );
 *     }
 * }
 * }</pre>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see EmailServiceImpl
 * @see Context
 */
public interface EmailService {

    /**
     * Sends a plain text email to the specified recipient.
     *
     * <p>This method is suitable for simple notifications that don't require
     * rich formatting. For styled emails with HTML content, use
     * {@link #sendEmailWithTemplate(String, String, String, Context)} instead.</p>
     *
     * <p><b>Use Cases:</b></p>
     * <ul>
     *   <li>System alerts and notifications</li>
     *   <li>Simple confirmation messages</li>
     *   <li>Plain text fallback when HTML is not supported</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * emailService.sendEmail(
     *         "user@example.com",
     *         "Account Update",
     *         "Your account has been successfully updated."
     * );
     * }</pre>
     *
     * @param to The recipient's email address (must be valid format)
     * @param subject The email subject line
     * @param text The plain text body of the email
     */
    void sendEmail(String to, String subject, String text);

    /**
     * Sends an HTML email rendered from a Thymeleaf template.
     *
     * <p>This method processes a Thymeleaf template with the provided context
     * variables and sends the resulting HTML as an email. This is the preferred
     * method for user-facing communications that require styling and branding.</p>
     *
     * <p><b>Template Location:</b></p>
     * Templates should be placed in the classpath at:
     * {@code src/main/resources/templates/}
     *
     * <p><b>Context Variables:</b></p>
     * The {@link Context} object contains variables that will be available
     * in the Thymeleaf template using {@code ${variableName}} syntax.
     *
     * <p><b>Example Template ({@code welcome.html}):</b></p>
     * <pre>{@code
     * <!DOCTYPE html>
     * <html>
     * <body>
     *     <h1>Welcome, [[${userName}]]!</h1>
     *     <p>Thank you for joining StayHub.</p>
     * </body>
     * </html>
     * }</pre>
     *
     * <p><b>Example Usage:</b></p>
     * <pre>{@code
     * Context context = new Context();
     * context.setVariable("userName", user.getFullName());
     * context.setVariable("activationLink", activationUrl);
     *
     * emailService.sendEmailWithTemplate(
     *         user.getEmail(),
     *         "Welcome to StayHub!",
     *         "welcome",
     *         context
     * );
     * }</pre>
     *
     * <p><b>Character Encoding:</b> UTF-8 is used to support international characters.</p>
     *
     * @param to The recipient's email address (must be valid format)
     * @param subject The email subject line (supports Unicode/UTF-8)
     * @param templateName The name of the Thymeleaf template without {@code .html} extension
     * @param context The Thymeleaf context containing template variables
     */
    void sendEmailWithTemplate(String to, String subject, String templateName, Context context);
}
package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.exception.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Implementation of the {@link EmailService} interface using Spring Mail.
 *
 * <p>This service handles email delivery via SMTP using Spring's
 * {@link JavaMailSender}. It supports both plain text emails and
 * HTML emails rendered from Thymeleaf templates.</p>
 *
 * <p><b>Configuration Required:</b></p>
 * The following properties must be configured in {@code application.yml}
 * or {@code application.properties}:
 *
 * <pre>{@code
 * spring:
 *   mail:
 *     host: smtp.gmail.com
 *     port: 587
 *     username: your-email@gmail.com
 *     password: your-app-password
 *     properties:
 *       mail:
 *         smtp:
 *           auth: true
 *           starttls:
 *             enable: true
 * }</pre>
 *
 * <p><b>Dependencies Required:</b></p>
 * <ul>
 *   <li>{@code spring-boot-starter-mail}</li>
 *   <li>{@code spring-boot-starter-thymeleaf}</li>
 * </ul>
 *
 * <p><b>Error Handling Strategy:</b></p>
 * <ul>
 *   <li>Template email failures throw {@link EmailSendException}</li>
 *   <li>Plain text email failures are logged but not rethrown (silent failure)</li>
 *   <li>All email operations are logged for monitoring and debugging</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. The {@link JavaMailSender}
 * and {@link TemplateEngine} are both thread-safe Spring-managed beans.</p>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see EmailService
 * @see JavaMailSender
 * @see TemplateEngine
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    /**
     * The email address used as the sender (From: field).
     * Injected from {@code spring.mail.username} configuration property.
     */
    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends a plain text email to the specified recipient.
     *
     * <p>This method uses Spring's {@link SimpleMailMessage} for simple
     * text-only emails. Failures are logged but not rethrown to avoid
     * disrupting the main application flow.</p>
     *
     * <p><b>⚠️ Error Handling Note:</b> Unlike {@link #sendEmailWithTemplate},
     * this method does not throw exceptions. Failures are silently logged.
     * Consider updating this behavior if guaranteed delivery is required.</p>
     *
     * <p><b>Example Log Output:</b></p>
     * <pre>
     * INFO: Sending email to user@example.com
     * DEBUG: Email sent to user@example.com successfully.
     * </pre>
     *
     * @param to The recipient's email address
     * @param subject The email subject line
     * @param text The plain text body of the email
     */
    @Override
    public void sendEmail(String to, String subject, String text) {
        log.info("Sending plain text email to: {}", to);
        log.debug("Subject: {}", subject);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            emailSender.send(message);
            log.debug("Plain text email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send plain text email to {}: {}", to, e.getMessage(), e);
            // Exception is not rethrown to avoid breaking the main flow
            // Consider adding to a retry queue or alerting monitoring system
        }
    }

    /**
     * Sends an HTML email rendered from a Thymeleaf template.
     *
     * <p>This method processes a Thymeleaf template with the provided context,
     * generates an HTML email, and sends it via SMTP. The email is sent as
     * multipart/related with UTF-8 encoding to support international characters.</p>
     *
     * <p><b>Processing Flow:</b></p>
     * <ol>
     *   <li>Process Thymeleaf template with provided context variables</li>
     *   <li>Create a MIME message with HTML content</li>
     *   <li>Set UTF-8 encoding for proper character support</li>
     *   <li>Send it via a configured SMTP server</li>
     * </ol>
     *
     * <p><b>Error Handling:</b></p>
     * Failures are wrapped in {@link EmailSendException} and rethrown to
     * allow upstream handlers to implement retry logic or notify administrators.</p>
     *
     * <p><b>Template Resolution:</b></p>
     * The template is resolved from the classpath:
     * {@code src/main/resources/templates/{templateName}.html}
     *
     * <p><b>Example Log Output:</b></p>
     * <pre>
     * INFO: Sending template email 'reservation-confirmation' to guest@example.com
     * DEBUG: Template email sent to guest@example.com successfully.
     * </pre>
     *
     * <p><b>Common Exceptions:</b></p>
     * <ul>
     *   <li><b>MessagingException:</b> SMTP connection issues, authentication failures</li>
     *   <li><b>TemplateProcessingException:</b> Template not found, syntax errors in template</li>
     *   <li><b>IllegalArgumentException:</b> Invalid email address format</li>
     * </ul>
     *
     * @param to The recipient's email address (must be valid format)
     * @param subject The email subject line (supports Unicode/UTF-8)
     * @param templateName The name of the Thymeleaf template without {@code .html} extension
     * @param context The Thymeleaf context containing template variables
     * @throws EmailSendException if the email fails to send it due to network,
     *                            authentication, or template processing errors
     */
    @Override
    public void sendEmailWithTemplate(String to, String subject, String templateName, Context context) {
        log.info("Sending template email '{}' to: {}", templateName, to);
        log.debug("Subject: {}, Template: {}", subject, templateName);

        try {
            // Process Thymeleaf template to HTML
            String html = templateEngine.process(templateName, context);
            log.debug("Template '{}' processed successfully", templateName);

            // Create MIME message with HTML content
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // true indicates HTML content

            // Send the email
            emailSender.send(message);
            log.debug("Template email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send template email to {}: Messaging error - {}",
                    to, e.getMessage(), e);
            throw new EmailSendException("Failed to send email to " + to +
                    " (messaging error)", e);

        } catch (org.thymeleaf.exceptions.TemplateProcessingException e) {
            log.error("Failed to process email template '{}' for recipient {}: {}",
                    templateName, to, e.getMessage(), e);
            throw new EmailSendException("Failed to process email template: " +
                    templateName, e);

        } catch (Exception e) {
            log.error("Unexpected error sending template email to {}: {}",
                    to, e.getMessage(), e);
            throw new EmailSendException("Failed to send email to " + to, e);
        }
    }

    /*
     * Additional methods that could be added in the future:
     *
     * // Send email with attachments
     * public void sendEmailWithAttachment(String to, String subject,
     *                                     String text, File attachment) {
     *     // Implementation using MimeMessageHelper.addAttachment()
     * }
     *
     * // Send email to multiple recipients
     * public void sendBulkEmail(List<String> recipients, String subject,
     *                           String templateName, Context context) {
     *     // Implementation with batch processing
     * }
     *
     * // Send email with inline images
     * public void sendEmailWithInlineImages(String to, String subject,
     *                                       String templateName, Context context,
     *                                       Map<String, File> inlineImages) {
     *     // Implementation using MimeMessageHelper.addInline()
     * }
     *
     * // Validate email address format
     * private boolean isValidEmail(String email) {
     *     // RFC 5322 email validation
     * }
     *
     * // Retry failed emails
     * @Async
     * @Retryable(value = EmailSendException.class, maxAttempts = 3)
     * public void sendEmailWithRetry(String to, String subject,
     *                                String templateName, Context context) {
     *     // Implementation with retry logic
     * }
     */
}
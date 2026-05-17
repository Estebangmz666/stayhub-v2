package edu.uniquindio.stayhub_v2.listener;

import edu.uniquindio.stayhub_v2.event.ReservationCompletedEvent;
import edu.uniquindio.stayhub_v2.event.ReservationCreatedEvent;
import edu.uniquindio.stayhub_v2.event.ReservationCancelledEvent;
import edu.uniquindio.stayhub_v2.exception.EmailNotificationException;
import edu.uniquindio.stayhub_v2.model.Accommodation;
import edu.uniquindio.stayhub_v2.model.Reservation;
import edu.uniquindio.stayhub_v2.model.User;
import edu.uniquindio.stayhub_v2.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Event listener for handling reservation-related email notifications.
 *
 * <p>This listener is triggered after a reservation is successfully created
 * and committed to the database. It sends confirmation emails to both the
 * guest and the host asynchronously to avoid blocking the main thread.</p>
 *
 * <p><b>Error Handling Strategy:</b></p>
 * <ul>
 *   <li>Email failures are logged but don't affect the reservation flow</li>
 *   <li>Individual email failures don't prevent the other email from being sent</li>
 *   <li>All errors are wrapped in custom exceptions with contextual information</li>
 * </ul>
 *
 * @author StayHub Dev Team
 * @version 1.0
 */
@Component @RequiredArgsConstructor @Slf4j
public class ReservationEmailListener {

    private final EmailService emailService;

    @Value("${stayhub.frontend.url}")
    private String frontendUrl;

    /**
     * Handles the reservation-created event by sending confirmation emails
     * to both the guest and the host.
     *
     * <p>This method is executed asynchronously and only after the transaction
     * has been successfully committed, ensuring that emails are only sent for
     * confirmed reservations.</p>
     *
     * @param event The reservation created event containing the reservation details
     */
    @Async
    @EventListener
    public void handleReservationCreated(ReservationCreatedEvent event) {
        log.info("Processing reservation email notifications for reservation ID: {}",
                event.reservation().getId());

        try {
            Reservation reservation = validateEventData(event);
            User guest = reservation.getGuest();
            Accommodation accommodation = reservation.getAccommodation();
            User host = accommodation.getHost();

            validateEmailData(guest, host, accommodation, reservation);

            log.debug("Guest email: {}, Host email: {}",
                    guest.getEmail(), host.getEmail());

            CompletableFuture<Void> guestEmailFuture = CompletableFuture.runAsync(() ->
                    sendGuestEmailSafely(guest, accommodation, reservation)
            );

            //10 delays secs for mailtrap purposes xd
            guestEmailFuture.join();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            CompletableFuture<Void> hostEmailFuture = CompletableFuture.runAsync(() ->
                    sendHostEmailSafely(host, guest, accommodation, reservation)
            );

            CompletableFuture.allOf(guestEmailFuture, hostEmailFuture).join();

            log.info("Email notification process completed for reservation ID: {}",
                    reservation.getId());

        } catch (Exception e) {
            log.error("Critical error in email notification handler for reservation: {}. Error: {}",
                    event.reservation().getId(),
                    e.getMessage(), e);
        }
    }

    @Async
    @EventListener
    public void handleReservationCancelled(ReservationCancelledEvent event) {
        log.info("Processing cancellation email notification for reservation ID: {}",
                event.reservation().getId());

        try {
            Reservation reservation = event.reservation();
            User guest = reservation.getGuest();
            Accommodation accommodation = reservation.getAccommodation();
            User host = accommodation.getHost();

            validateEmailData(guest, host, accommodation, reservation);
            sendCancellationEmailToHostSafely(host, guest, accommodation, reservation);

            log.info("Cancellation email notification completed for reservation ID: {}",
                    reservation.getId());
        } catch (Exception e) {
            log.error("Critical error in cancellation email notification handler for reservation: {}. Error: {}",
                    event.reservation().getId(), e.getMessage(), e);
        }
    }

    @Async
    @EventListener
    public void handleReservationCompleted(ReservationCompletedEvent event) {
        log.info("Processing reservation completed email notification for reservation ID: {}",
                event.reservation().getId());

        try {
            Reservation reservation = event.reservation();
            User guest = reservation.getGuest();
            Accommodation accommodation = reservation.getAccommodation();
            User host = accommodation.getHost();

            validateEmailData(guest, host, accommodation, reservation);

            sendCompletedReservationEmailToGuestSafely(guest, accommodation, reservation);
            sendCompletedReservationEmailToHostSafely(host, guest, accommodation, reservation);

            log.info("Reservation completed email notification completed for reservation ID: {}",
                    reservation.getId());
        } catch (Exception e) {
            log.error("Critical error in reservationCompleted email notification handler for reservation: {}. Error: {}",
                    event.reservation().getId(), e.getMessage(), e);
        }
    }

    private void sendCompletedReservationEmailToGuestSafely(
            User guest,
            Accommodation accommodation,
            Reservation reservation) {

        log.info("Attempting to send completed-stay email to guest: {}", guest.getEmail());

        try {
            validateEmailRecipient(guest);

            Context context = buildCompletedReservationGuestEmailContext(
                    guest,
                    accommodation,
                    reservation
            );

            emailService.sendEmailWithTemplate(
                    guest.getEmail(),
                    "Tu estadía en " + accommodation.getTitle() + " ha finalizado",
                    "reservation-completed-guest",
                    context
            );

            log.info("Completed-stay email sent successfully to guest: {}", guest.getEmail());
        } catch (EmailNotificationException e) {
            log.error("Failed to send completed-stay email to guest {} for reservation {}: {}",
                    guest.getEmail(), reservation.getId(), e.getMessage());
            handleEmailFailure(guest, reservation, "guest completed-stay notification");
        } catch (Exception e) {
            log.error("Unexpected error sending completed-stay email to guest {} for reservation {}: {}",
                    guest.getEmail(), reservation.getId(), e.getMessage(), e);
            throw new EmailNotificationException(
                    String.format("Unexpected error sending completed-stay email to guest %s",
                            guest.getEmail()), e);
        }
    }

    private void sendCompletedReservationEmailToHostSafely(
            User host,
            User guest,
            Accommodation accommodation,
            Reservation reservation) {

        log.info("Attempting to send completed-stay notification email to host: {}", host.getEmail());

        try {
            validateEmailRecipient(host);

            Context context = buildCompletedReservationHostEmailContext(
                    host,
                    guest,
                    accommodation,
                    reservation
            );

            emailService.sendEmailWithTemplate(
                    host.getEmail(),
                    "La reserva en " + accommodation.getTitle() + " ha finalizado",
                    "reservation-completed-host",
                    context
            );

            log.info("Completed-stay notification email sent successfully to host: {}", host.getEmail());
        } catch (EmailNotificationException e) {
            log.error("Failed to send completed-stay email to host {} for reservation {}: {}",
                    host.getEmail(), reservation.getId(), e.getMessage());
            handleEmailFailure(host, reservation, "host completed-stay notification");
        } catch (Exception e) {
            log.error("Unexpected error sending completed-stay email to host {} for reservation {}: {}",
                    host.getEmail(), reservation.getId(), e.getMessage(), e);
            throw new EmailNotificationException(
                    String.format("Unexpected error sending completed-stay email to host %s",
                            host.getEmail()), e);
        }
    }

    /**
     * Sends the confirmation email to the guest with comprehensive error handling.
     *
     * @param guest The guest user
     * @param accommodation The booked accommodation
     * @param reservation The reservation details
     */
    private void sendGuestEmailSafely(User guest, Accommodation accommodation, Reservation reservation) {

        log.info("📤 Attempting to send confirmation email to guest: {}", guest.getEmail());

        try {
            validateEmailRecipient(guest);

            Context context = buildGuestEmailContext(guest, accommodation, reservation);

            emailService.sendEmailWithTemplate(
                    guest.getEmail(),
                    "Tu reserva en " + accommodation.getTitle() + " está confirmada",
                    "reservation-confirmation",
                    context
            );

            log.info("Confirmation email sent successfully to guest: {}", guest.getEmail());

        } catch (EmailNotificationException e) {
            log.error("Failed to send email to guest {} for reservation {}: {}",
                    guest.getEmail(), reservation.getId(), e.getMessage());
            handleEmailFailure(guest, reservation, "guest confirmation");

        } catch (Exception e) {
            log.error("Unexpected error sending email to guest {} for reservation {}: {}",
                    guest.getEmail(), reservation.getId(), e.getMessage(), e);
            throw new EmailNotificationException(
                    String.format("Unexpected error sending confirmation email to guest %s",
                            guest.getEmail()), e);
        }
    }

    /**
     * Sends the notification email to the host with comprehensive error handling.
     *
     * @param host The host user
     * @param guest The guest who made the reservation
     * @param accommodation The booked accommodation
     * @param reservation The reservation details
     */
    private void sendHostEmailSafely(User host, User guest, Accommodation accommodation,
                                     Reservation reservation) {

        log.info("Attempting to send notification email to host: {}", host.getEmail());

        try {
            validateEmailRecipient(host);

            Context context = buildHostEmailContext(host, guest, accommodation, reservation);

            emailService.sendEmailWithTemplate(
                    host.getEmail(),
                    "Nueva reserva en " + accommodation.getTitle(),
                    "host-reservation-notification",
                    context
            );

            log.info("Notification email sent successfully to host: {}", host.getEmail());

        } catch (EmailNotificationException e) {
            log.error("Failed to send email to host {} for reservation {}: {}",
                    host.getEmail(), reservation.getId(), e.getMessage());
            handleEmailFailure(host, reservation, "host notification");

        } catch (Exception e) {
            log.error("Unexpected error sending email to host {} for reservation {}: {}",
                    host.getEmail(), reservation.getId(), e.getMessage(), e);
            throw new EmailNotificationException(
                    String.format("Unexpected error sending notification email to host %s",
                            host.getEmail()), e);
        }
    }

    private void sendCancellationEmailToHostSafely(
            User host,
            User guest,
            Accommodation accommodation,
            Reservation reservation) {

        log.info("Attempting to send cancellation notification email to host: {}", host.getEmail());

        try {
            validateEmailRecipient(host);

            Context context = buildHostEmailContext(host, guest, accommodation, reservation);

            emailService.sendEmailWithTemplate(
                    host.getEmail(),
                    "Reserva cancelada en " + accommodation.getTitle(),
                    "host-reservation-cancellation",
                    context
            );

            log.info("Cancellation notification email sent successfully to host: {}", host.getEmail());
        } catch (EmailNotificationException e) {
            log.error("Failed to send cancellation email to host {} for reservation {}: {}",
                    host.getEmail(), reservation.getId(), e.getMessage());
            handleEmailFailure(host, reservation, "host cancellation notification");
        } catch (Exception e) {
            log.error("Unexpected error sending cancellation email to host {} for reservation {}: {}",
                    host.getEmail(), reservation.getId(), e.getMessage(), e);
            throw new EmailNotificationException(
                    String.format("Unexpected error sending cancellation email to host %s",
                            host.getEmail()), e);
        }
    }

    /**
     * Builds the Thymeleaf context for the guest email template.
     */
    private Context buildGuestEmailContext(User guest, Accommodation accommodation,
                                           Reservation reservation) {
        Context context = new Context();

        try {
            context.setVariable("userName", guest.getFullName() != null ?
                    guest.getFullName() : "Huésped");
            context.setVariable("accommodationTitle", accommodation.getTitle());
            context.setVariable("startDate", formatDate(reservation.getStartDate()));
            context.setVariable("endDate", formatDate(reservation.getEndDate()));
            context.setVariable("totalPrice", formatPrice(reservation.getTotalPrice()));

            log.debug("Guest email context built successfully for user: {}", guest.getEmail());

        } catch (Exception e) {
            log.error("Error building guest email context: {}", e.getMessage(), e);
            throw new EmailNotificationException("Failed to build guest email context", e);
        }

        return context;
    }

    /**
     * Builds the Thymeleaf context for the host email template.
     */
    private Context buildHostEmailContext(User host, User guest, Accommodation accommodation,
                                          Reservation reservation) {
        Context context = new Context();

        try {
            context.setVariable("hostName", host.getFullName() != null ?
                    host.getFullName() : "Anfitrión");
            context.setVariable("guestName", guest.getFullName() != null ?
                    guest.getFullName() : "Huésped");
            context.setVariable("accommodationTitle", accommodation.getTitle());
            context.setVariable("startDate", formatDate(reservation.getStartDate()));
            context.setVariable("endDate", formatDate(reservation.getEndDate()));

            log.debug("Host email context built successfully for user: {}", host.getEmail());

        } catch (Exception e) {
            log.error("Error building host email context: {}", e.getMessage(), e);
            throw new EmailNotificationException("Failed to build host email context", e);
        }

        return context;
    }

    private Context buildCompletedReservationGuestEmailContext(
            User guest,
            Accommodation accommodation,
            Reservation reservation) {
        Context context = new Context();

        try {
            context.setVariable("guestName", guest.getFullName() != null
                    ? guest.getFullName()
                    : "Huésped");
            context.setVariable("accommodationTitle", accommodation.getTitle());
            context.setVariable("startDate", formatDate(reservation.getStartDate()));
            context.setVariable("endDate", formatDate(reservation.getEndDate()));
            context.setVariable("reservationId", reservation.getId());
            context.setVariable("reviewUrl", buildReviewUrl(reservation));

            log.debug("Completed guest email context built successfully for user: {}", guest.getEmail());
        } catch (Exception e) {
            log.error("Error building completed guest email context: {}", e.getMessage(), e);
            throw new EmailNotificationException("Failed to build completed guest email context", e);
        }

        return context;
    }

    private Context buildCompletedReservationHostEmailContext(
            User host,
            User guest,
            Accommodation accommodation,
            Reservation reservation) {
        Context context = new Context();

        try {
            context.setVariable("hostName", host.getFullName() != null
                    ? host.getFullName()
                    : "Anfitrión");
            context.setVariable("guestName", guest.getFullName() != null
                    ? guest.getFullName()
                    : "Huésped");
            context.setVariable("accommodationTitle", accommodation.getTitle());
            context.setVariable("startDate", formatDate(reservation.getStartDate()));
            context.setVariable("endDate", formatDate(reservation.getEndDate()));
            context.setVariable("reservationId", reservation.getId());

            log.debug("Completed host email context built successfully for user: {}", host.getEmail());
        } catch (Exception e) {
            log.error("Error building completed host email context: {}", e.getMessage(), e);
            throw new EmailNotificationException("Failed to build completed host email context", e);
        }

        return context;
    }

    private String buildReviewUrl(Reservation reservation) {
        return String.format(
                "%s/dashboard/guest/reviews?accommodationId=%d&reservationId=%d",
                frontendUrl,
                reservation.getAccommodation().getId(),
                reservation.getId()
        );
    }

    /**
     * Validates that all required data from the event is present.
     */
    private Reservation validateEventData(ReservationCreatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("ReservationCreatedEvent cannot be null");
        }

        Reservation reservation = event.reservation();
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation in event cannot be null");
        }

        if (reservation.getId() == null) {
            throw new IllegalArgumentException("Reservation ID cannot be null");
        }

        return reservation;
    }

    /**
     * Validates that all required entities have valid email addresses.
     */
    private void validateEmailData(User guest, User host, Accommodation accommodation,
                                   Reservation reservation) {
        if (guest == null || host == null || accommodation == null) {
            throw new IllegalArgumentException(
                    String.format("Missing required data for reservation %s: guest=%s, host=%s, accommodation=%s",
                            reservation.getId(),
                            guest != null ? "present" : "missing",
                            host != null ? "present" : "missing",
                            accommodation != null ? "present" : "missing"
                    ));
        }
    }

    /**
     * Validates that a user has a valid email address.
     */
    private void validateEmailRecipient(User user) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new EmailNotificationException(
                    String.format("User %s has no valid email address", user.getId()));
        }

        String email = user.getEmail().trim();
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            log.warn("Potentially invalid email format: {}", email);
        }
    }

    /**
     * Handles email sending failures with appropriate logging and fallback strategies.
     */
    private void handleEmailFailure(User user, Reservation reservation, String emailType) {

        log.warn("Email delivery failed for {} to user {} (reservation {}). " +
                        "This does not affect the reservation status.",
                emailType, user.getEmail(), reservation.getId());
    }

    /**
     * Formats a date for email display.
     */
    private String formatDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "Fecha no disponible";

        try {
            java.time.format.DateTimeFormatter formatter =
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return dateTime.format(formatter);
        } catch (Exception e) {
            log.warn("Error formatting date: {}", e.getMessage());
            return dateTime.toString();
        }
    }

    /**
     * Formats a price for email display.
     */
    private String formatPrice(java.math.BigDecimal price) {
        if (price == null) return "Precio no disponible";
        try {
            java.text.NumberFormat currencyFormat =
                    java.text.NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));
            return currencyFormat.format(price);
        } catch (Exception e) {
            log.warn("Error formatting price: {}", e.getMessage());
            return "$" + price;
        }
    }
}

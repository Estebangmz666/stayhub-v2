package edu.uniquindio.stayhub_v2.scheduler;

import edu.uniquindio.stayhub_v2.model.Reservation;
import edu.uniquindio.stayhub_v2.repository.ReservationRepository;
import edu.uniquindio.stayhub_v2.service.EmailService;
import edu.uniquindio.stayhub_v2.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Scheduled task component responsible for sending payment reminder emails
 * to guests whose reservation deposit deadline is approaching.
 *
 * <p>
 * Runs daily at 09:00 AM and checks for all active reservations where:
 * <ul>
 * <li>The deposit has NOT been paid ({@code depositPaid = false})</li>
 * <li>The payment deadline falls within the next 24 hours</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Schedule:</b> {@code 0 0 9 * * ?} — every day at 09:00 AM
 * </p>
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentReminderScheduler {

    private final ReservationRepository reservationRepository;
    private final EmailService emailService;
    private final ReservationService reservationService;

    @Value("${stayhub.payment.bank-account}")
    private String bankAccountNumber;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Sends payment reminder emails to guests whose deposit deadline is within 24
     * hours.
     *
     * <p>
     * This method is executed automatically every day at 09:00 AM. It queries
     * reservations that are approaching their payment deadline and sends a reminder
     * email to each guest using the {@code payment-reminder} Thymeleaf template.
     * </p>
     *
     * <p>
     * Email failures per individual reservation are caught and logged so that
     * a single failure does not prevent the remaining reminders from being sent.
     * </p>
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendPaymentReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24Hours = now.plusHours(24);

        log.info("Running payment reminder scheduler. Window: {} to {}", now, in24Hours);

        List<Reservation> approaching = reservationRepository
                .findReservationsWithPaymentDeadlineApproaching(now, in24Hours);

        if (approaching.isEmpty()) {
            log.info("No reservations with approaching payment deadlines found.");
            return;
        }

        log.info("Found {} reservation(s) with approaching payment deadlines.", approaching.size());

        approaching.forEach(reservation -> {
            try {
                sendReminderEmail(reservation);
            } catch (Exception e) {
                log.error("Failed to send payment reminder for reservation ID {}: {}",
                        reservation.getId(), e.getMessage(), e);
            }
        });

        log.info("Payment reminder scheduler completed.");
    }

    @Scheduled(cron = "0 15 0 * * ?")
    public void cancelExpiredUnpaidReservations() {
        log.info("Running expired unpaid reservation cancellation scheduler.");

        int cancelledReservations = reservationService.cancelExpiredUnpaidReservations();

        log.info("Expired unpaid reservation cancellation scheduler completed. Cancelled reservations: {}",
                cancelledReservations);
    }

    /**
     * Sends the payment reminder email to the guest of the given reservation.
     *
     * @param reservation The reservation with an approaching payment deadline
     */
    private void sendReminderEmail(Reservation reservation) {
        String guestEmail = reservation.getGuest().getEmail();
        String guestName = reservation.getGuest().getFullName() != null
                ? reservation.getGuest().getFullName()
                : "Huésped";
        String accommodationTitle = reservation.getAccommodation().getTitle();
        BigDecimal depositAmount = reservation.getDepositAmount();
        LocalDateTime deadline = reservation.getPaymentDeadline();

        log.debug("Sending payment reminder to {} for reservation ID {}",
                guestEmail, reservation.getId());

        Context context = new Context();
        context.setVariable("guestName", guestName);
        context.setVariable("reservationId", reservation.getId());
        context.setVariable("accommodationTitle", accommodationTitle);
        context.setVariable("depositAmount", formatCurrency(depositAmount));
        context.setVariable("bankAccountNumber", bankAccountNumber);
        context.setVariable("paymentDeadline", deadline.format(DATE_FORMATTER));

        emailService.sendEmailWithTemplate(
                guestEmail,
                "Recordatorio: Anticipo pendiente para tu reserva en " + accommodationTitle,
                "payment-reminder",
                context);

        log.info("Payment reminder sent to {} for reservation ID {}", guestEmail, reservation.getId());
    }

    /**
     * Formats a BigDecimal amount as Colombian peso currency string.
     *
     * @param amount The amount to format
     * @return Formatted currency string, e.g. "$200.000"
     */
    private String formatCurrency(BigDecimal amount) {
        if (amount == null)
            return "$0";
        try {
            NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
            return fmt.format(amount);
        } catch (Exception e) {
            log.warn("Error formatting currency amount: {}", e.getMessage());
            return "$" + amount.toPlainString();
        }
    }
}

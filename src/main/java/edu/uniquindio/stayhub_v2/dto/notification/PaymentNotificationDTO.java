package edu.uniquindio.stayhub_v2.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

/**
 * Data Transfer Object for in-app payment pending notifications.
 *
 * <p>Returned when querying pending payment notifications for the authenticated guest.
 * The frontend can use this DTO to display an in-app alert reminding the guest to
 * complete the 20% deposit within the established deadline.</p>
 *
 * @param reservationId      The unique identifier of the reservation.
 * @param accommodationTitle The name of the accommodation that was booked.
 * @param depositAmount      The pending deposit amount (20% of total price).
 * @param currency           The currency in which the deposit is expressed.
 * @param bankAccountNumber  The bank account where the transfer must be made.
 * @param paymentDeadline    The deadline by which the deposit must be paid.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "In-app notification response for a pending advance payment on a reservation.")
public record PaymentNotificationDTO(

        @Schema(description = "Unique identifier of the reservation", example = "12345")
        Long reservationId,

        @Schema(description = "Title of the booked accommodation", example = "Cabaña en el Quindío")
        String accommodationTitle,

        @Schema(description = "Pending deposit amount (20% of total price)", example = "200.00")
        BigDecimal depositAmount,

        @Schema(description = "Currency of the deposit amount", example = "COP")
        Currency currency,

        @Schema(description = "Bank account number for the transfer", example = "3001234567890")
        String bankAccountNumber,

        @Schema(description = "Deadline to complete the payment", example = "2025-04-18T23:59:59")
        LocalDateTime paymentDeadline

) {}

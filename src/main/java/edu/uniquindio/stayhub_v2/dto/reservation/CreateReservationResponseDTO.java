package edu.uniquindio.stayhub_v2.dto.reservation;

import edu.uniquindio.stayhub_v2.model.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

/**
 * Data Transfer Object representing the response for the creation of a
 * reservation.
 *
 * <p>
 * This record encapsulates all the details related to a newly created
 * reservation,
 * including payment information required for the 20% advance deposit.
 * </p>
 *
 * @param id                 The unique identifier of the reservation.
 * @param startDate          The start date and time of the reservation.
 * @param endDate            The end date and time of the reservation.
 * @param totalPrice         The total price calculated for the reservation.
 * @param currency           The currency in which the total price is expressed.
 * @param status             The current status of the reservation.
 * @param accommodationId    The unique identifier of the associated
 *                           accommodation.
 * @param accommodationTitle The title or name of the associated accommodation.
 * @param userId             The unique identifier of the user who made the
 *                           reservation.
 * @param depositAmount      The required advance payment (20% of totalPrice) to
 *                           confirm the reservation.
 * @param bankAccountNumber  The bank account number where the guest must
 *                           transfer the deposit.
 * @param paymentDeadline    The deadline (3 days from booking) by which the
 *                           deposit must be paid.
 *
 * @author Esteban Gómez León
 * @version 1.1
 */
@Schema(description = "Data Transfer Object representing the response for the creation of a reservation, including payment details.")
public record CreateReservationResponseDTO(

                @NotNull @Schema(description = "Unique identifier of the reservation", example = "12345") Long id,

                @NotNull @Schema(description = "Check-in date and time", example = "2025-06-01T15:00:00") LocalDateTime startDate,

                @NotNull @Schema(description = "Check-out date and time", example = "2025-06-05T11:00:00") LocalDateTime endDate,

                @NotNull @DecimalMin(value = "0.0", inclusive = false) @Schema(description = "Total price for the entire stay", example = "1000.00") BigDecimal totalPrice,

                @NotNull @Schema(description = "Currency of the total price", example = "COP") Currency currency,

                @NotNull @Schema(description = "Current status of the reservation", example = "ACTIVE") ReservationStatus status,

                @NotNull @Schema(description = "ID of the booked accommodation", example = "1") Long accommodationId,

                @NotNull @Schema(description = "Title of the booked accommodation", example = "Beachfront Villa with Pool") String accommodationTitle,

                @NotNull @Schema(description = "ID of the guest who made the reservation", example = "678") Long userId,

                @NotNull @DecimalMin(value = "0.0", inclusive = false) @Schema(description = "Required advance deposit (20% of total price). Must be paid within the payment deadline.", example = "200.00") BigDecimal depositAmount,

                @NotNull @Schema(description = "Bank account number where the deposit must be transferred", example = "3001234567890") String bankAccountNumber,

                @NotNull @Schema(description = "Deadline to pay the deposit (3 days from reservation creation)", example = "2025-04-18T23:59:59") LocalDateTime paymentDeadline

) {
}
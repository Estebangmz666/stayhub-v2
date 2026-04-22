package edu.uniquindio.stayhub_v2.dto.reservation;

import edu.uniquindio.stayhub_v2.model.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object used as the response body when retrieving the complete
 * details of a reservation.
 *
 * <p>
 * This record exposes reservation dates, accommodation information, guest
 * information, payment summary, deposit status, and audit timestamps required
 * by clients that need to display or inspect a reservation in detail.
 * </p>
 *
 * @param id                 Unique identifier of the reservation.
 * @param startDate          Check-in date and time selected for the reservation.
 * @param endDate            Check-out date and time selected for the reservation.
 * @param accommodationId    Unique identifier of the booked accommodation.
 * @param accommodationTitle Title of the booked accommodation.
 * @param accommodationCity  City where the booked accommodation is located.
 * @param guestId            Unique identifier of the guest who made the
 *                           reservation.
 * @param guestEmail         Email address of the guest who made the reservation.
 * @param totalPrice         Total price calculated for the complete stay.
 * @param currency           Currency in which the total price and deposit are
 *                           expressed.
 * @param depositAmount      Required advance deposit amount for the reservation.
 * @param depositPaid        Flag that indicates whether the required deposit has
 *                           already been paid.
 * @param paymentDeadline    Deadline by which the guest must pay the deposit.
 * @param status             Current status of the reservation.
 * @param createdAt          Date and time when the reservation was created.
 * @param updatedAt          Date and time when the reservation was last updated.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(
        name = "RetrieveReservationResponse",
        description = "Detailed response returned when retrieving a reservation, including stay dates, accommodation data, guest data, payment summary, deposit status, and audit timestamps."
)
public record RetrieveReservationResponseDTO(

        @NotNull @Schema(description = "Unique identifier of the reservation", example = "12345")
        Long id,

        @NotNull @Schema(description = "Check-in date and time", example = "2025-06-01T15:00:00")
        LocalDateTime startDate,

        @NotNull @Schema(description = "Check-out date and time", example = "2025-06-05T11:00:00")
        LocalDateTime endDate,

        @NotNull @Schema(description = "ID of the booked accommodation", example = "1")
        Long accommodationId,

        @NotNull @Schema(description = "Title of the booked accommodation", example = "Beachfront Villa with Pool")
        String accommodationTitle,

        @NotNull @Schema(description = "City where the booked accommodation is located", example = "Medellín")
        String accommodationCity,

        @NotNull @Schema(description = "ID of the guest who made the reservation", example = "678")
        Long guestId,

        @NotNull @Schema(description = "Email of the guest who made the reservation", example = "johndoe@gmail.com")
        String guestEmail,

        @NotNull @Schema(description = "Total price calculated for the complete stay", example = "1250000")
        BigDecimal totalPrice,

        @NotNull @Schema(description = "Currency in which the total price and deposit are expressed", example = "COP")
        String currency,

        @NotNull @Schema(description = "Required advance deposit amount. Must be paid within the payment deadline.", example = "200000")
        BigDecimal depositAmount,

        @NotNull @Schema(description = "Flag that indicates whether the guest has already paid the required deposit", example = "true")
        Boolean depositPaid,

        @NotNull @Schema(description = "Last date and time when the guest can pay the reservation deposit", example = "2025-06-04T23:59:59")
        LocalDateTime paymentDeadline,

        @NotNull @Schema(description = "Current status of the reservation", example = "ACTIVE", allowableValues = {"ACTIVE", "CANCELLED", "COMPLETED"})
        ReservationStatus status,

        @NotNull @Schema(description = "Datetime that defines when the reservation was created", example = "2025-06-01T15:00:00")
        LocalDateTime createdAt,

        @NotNull @Schema(description = "Datetime that defines when the reservation was last updated", example = "2025-06-01T15:00:00")
        LocalDateTime updatedAt
) {}

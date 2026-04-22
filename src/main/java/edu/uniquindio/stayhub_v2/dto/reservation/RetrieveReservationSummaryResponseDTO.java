package edu.uniquindio.stayhub_v2.dto.reservation;

import edu.uniquindio.stayhub_v2.model.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object used to return compact reservation information in
 * paginated reservation lists.
 *
 * @param id                 Unique identifier of the reservation.
 * @param accommodationId    Unique identifier of the booked accommodation.
 * @param accommodationTitle Title of the booked accommodation.
 * @param startDate          Check-in date and time of the reservation.
 * @param endDate            Check-out date and time of the reservation.
 * @param totalPrice         Total price calculated for the reservation.
 * @param currency           Currency in which the total price is expressed.
 * @param status             Current status of the reservation.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Compact reservation response returned in reservation list views.")
public record RetrieveReservationSummaryResponseDTO(

        @NotNull @Schema(description = "Unique identifier of the reservation", example = "12345")
        Long id,

        @NotNull @Schema(description = "ID of the booked accommodation", example = "1")
        Long accommodationId,

        @NotNull @Schema(description = "Title of the booked accommodation", example = "Beachfront Villa with Pool")
        String accommodationTitle,

        @NotNull @Schema(description = "Check-in date and time", example = "2025-06-01T15:00:00")
        LocalDateTime startDate,

        @NotNull @Schema(description = "Check-out date and time", example = "2025-06-05T11:00:00")
        LocalDateTime endDate,

        @NotNull @Schema(description = "Total price calculated for the reservation", example = "1250000")
        BigDecimal totalPrice,

        @NotNull @Schema(description = "Currency in which the total price is expressed", example = "COP")
        String currency,

        @NotNull @Schema(description = "Current status of the reservation", example = "ACTIVE", allowableValues = {"ACTIVE", "CANCELLED", "COMPLETED"})
        ReservationStatus status
) {}

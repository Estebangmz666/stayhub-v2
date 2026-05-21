package edu.uniquindio.stayhub_v2.dto.reservation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Request the body used by guests to update the date range of an active
 * reservation.
 *
 * @param startDate New check-in date and time.
 * @param endDate   New check-out date and time.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Request body used to update the date range of an active reservation.")
public record UpdateReservationRequestDTO(
        @Schema(description = "New check-in date and time", example = "2025-06-03T15:00:00")
        @NotNull(message = "Start date is required")
        @Future(message = "Start date must be in the future")
        LocalDateTime startDate,

        @Schema(description = "New check-out date and time", example = "2025-06-07T11:00:00")
        @NotNull(message = "End date is required")
        @Future(message = "End date must be in the future")
        LocalDateTime endDate
) {
    public UpdateReservationRequestDTO {
        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }
}

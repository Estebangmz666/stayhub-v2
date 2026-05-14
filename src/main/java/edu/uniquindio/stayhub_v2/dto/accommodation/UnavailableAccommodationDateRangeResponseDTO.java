package edu.uniquindio.stayhub_v2.dto.accommodation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Response body that exposes a blocked date range for an accommodation.
 *
 * <p>
 * Each item represents one active reservation interval that currently makes
 * the accommodation unavailable for new bookings.
 * </p>
 *
 * @param startDate Check-in date and time that starts the unavailable interval.
 * @param endDate   Check-out date and time that ends the unavailable interval.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(
        name = "UnavailableAccommodationDateRangeResponse",
        description = "Blocked date range returned when listing the dates where an accommodation is not available."
)
public record UnavailableAccommodationDateRangeResponseDTO(

        @NotNull
        @Schema(description = "Check-in date and time that starts the blocked interval", example = "2026-06-10T15:00:00")
        LocalDateTime startDate,

        @NotNull
        @Schema(description = "Check-out date and time that ends the blocked interval", example = "2026-06-13T11:00:00")
        LocalDateTime endDate
) {
}

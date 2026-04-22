package edu.uniquindio.stayhub_v2.dto.reservation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for creating a reservation
 * This DTO contains all the necessary information to create a reservation.
 *
 * @param accommodationId The ID of the accommodation to reserve.
 * @param startDate The start date of the reservation.
 * @param endDate The end date of the reservation.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Request body used to create a reservation for an accommodation.")
public record CreateReservationRequestDTO(
        @Schema(description = "ID of the accommodation to reserve", example = "1")
        @NotNull(message = "Accommodation ID is required")
        Long accommodationId,

        @Schema(description = "Check-in date and time", example = "2025-06-01T15:00:00")
        @NotNull(message = "Start date is required")
        @Future(message = "Start date must be in the future")
        LocalDateTime startDate,

        @Schema(description = "Check-out date and time", example = "2025-06-05T11:00:00")
        @NotNull(message = "End date is required")
        @Future(message = "End date must be in the future")
        LocalDateTime endDate
)   {
    /**
     * Compact constructor for {@link CreateReservationRequestDTO}.
     * <p>
     * Performs additional validation beyond annotations to ensure that
     * the reservation date range is logically consistent.
     * </p>
     *
     * <p>
     * Specifically, it validates that:
     * <ul>
     *     <li>Both {@code startDate} and {@code endDate} are not null (handled by annotations).</li>
     *     <li>{@code endDate} occurs strictly after {@code startDate}.</li>
     * </ul>
     * </p>
     *
     * @param accommodationId the unique identifier of the accommodation to be reserved
     * @param startDate the check-in date and time of the reservation
     * @param endDate the check-out date and time of the reservation
     *
     * @throws IllegalArgumentException if {@code endDate} is not after {@code startDate}
     */
    public CreateReservationRequestDTO {
        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }
}
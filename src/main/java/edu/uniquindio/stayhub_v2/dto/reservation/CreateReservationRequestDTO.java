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
 * @author Esteban Gómez León
 * @version 1.0
 */
@Schema(description = "Data Transfer Object for creating a reservation")
public record CreateReservationRequestDTO(
        @Schema(description = "ID of the accommodation to reserve", example = "1")
        @NotNull(message = "Accommodation ID is required")
        Long accommodationId,

        @Schema(description = "Check-in date", example = "2025-06-01")
        @NotNull(message = "Start date is required")
        @Future(message = "Start date must be in the future")
        LocalDateTime startDate,

        @Schema(description = "Check-out date", example = "2025-06-05")
        @NotNull(message = "End date is required")
        @Future(message = "End date must be in the future")
        LocalDateTime endDate
) {
    public CreateReservationRequestDTO {
        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }
}
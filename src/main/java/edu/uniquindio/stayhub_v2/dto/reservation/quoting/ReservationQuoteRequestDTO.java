package edu.uniquindio.stayhub_v2.dto.reservation.quoting;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Request object for requesting a reservation quote")
public record ReservationQuoteRequestDTO(

        @NotNull(message = "Accommodation ID is required")
        @Schema(
                description = "ID of the accommodation to reserve",
                example = "12345",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long accommodationId,

        @NotNull(message = "Start date is required")
        @Future(message = "Start date must be in the future")
        @Schema(
                description = "Start date and time of the reservation",
                example = "2026-06-01T14:00:00",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        LocalDateTime startDate,

        @NotNull(message = "End date is required")
        @Future(message = "End date must be in the future")
        @Schema(
                description = "End date and time of the reservation",
                example = "2026-06-07T11:00:00",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        LocalDateTime endDate

) {
    public ReservationQuoteRequestDTO {
        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }
}
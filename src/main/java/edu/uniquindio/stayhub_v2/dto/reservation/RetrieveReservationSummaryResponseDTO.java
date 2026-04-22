package edu.uniquindio.stayhub_v2.dto.reservation;

import edu.uniquindio.stayhub_v2.model.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RetrieveReservationSummaryResponseDTO(

        @NotNull @Schema(description = "The unique identifier of the reservation", example = "12345")
        Long id,

        @NotNull @Schema(description = "The ID of the accommodation booked", example = "1")
        Long accommodationId,

        @NotNull @Schema(description = "The title of the accommodation booked", example = "Beachfront Villa with Pool")
        String accommodationTitle,

        @NotNull @Schema(description = "The start date for the reservation", example = "2025-06-01T15:00:00")
        LocalDateTime startDate,

        @NotNull @Schema(description = "The end date for the reservation", example = "2025-06-05T11:00:00")
        LocalDateTime endDate,

        @NotNull @Schema(description = "The total price of the reservation", example = "1250000")
        BigDecimal totalPrice,

        @NotNull @Schema(description = "The currency of the total price", example = "COP")
        String currency,

        @NotNull @Schema(description = "The current status of the reservation", example = "ACTIVE")
        ReservationStatus status
) {}
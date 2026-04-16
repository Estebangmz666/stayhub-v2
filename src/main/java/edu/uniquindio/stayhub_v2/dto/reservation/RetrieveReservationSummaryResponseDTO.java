package edu.uniquindio.stayhub_v2.dto.reservation;

import edu.uniquindio.stayhub_v2.model.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RetrieveReservationSummaryResponseDTO(

        @NotNull @Schema(description = "The unique identifier of the reservation")
        Long id,

        @NotNull @Schema(description = "The ID of the accommodation booked")
        Long accommodationId,

        @NotNull @Schema(description = "The title of the accommodation booked")
        String accommodationTitle,

        @NotNull @Schema(description = "The start date for the reservation")
        LocalDateTime startDate,

        @NotNull @Schema(description = "The end date for the reservation")
        LocalDateTime endDate,

        @NotNull @Schema(description = "The total price of the reservation")
        BigDecimal totalPrice,

        @NotNull @Schema(description = "The currency of the total price")
        String currency,

        @NotNull @Schema(description = "The current status of the reservation")
        ReservationStatus status
) {}
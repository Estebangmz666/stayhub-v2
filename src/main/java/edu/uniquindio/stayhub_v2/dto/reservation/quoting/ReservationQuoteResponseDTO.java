package edu.uniquindio.stayhub_v2.dto.reservation.quoting;

import edu.uniquindio.stayhub_v2.dto.reservation.RentalPriceModificationResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Response object with the complete reservation quote details")
public record ReservationQuoteResponseDTO(

        @Schema(
                description = "ID of the accommodation",
                example = "12345",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        Long accommodationId,

        @Schema(
                description = "Start date and time of the reservation",
                example = "2026-06-01T14:00:00",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        LocalDateTime startDate,

        @Schema(
                description = "End date and time of the reservation",
                example = "2026-06-07T11:00:00",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        LocalDateTime endDate,

        @Schema(
                description = "Number of nights of the stay",
                example = "6",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        int nights,

        @Schema(
                description = "Base total price before discounts or modifications",
                example = "600.00",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        BigDecimal baseTotalPrice,

        @Schema(
                description = "Final total price after applying all modifications and discounts",
                example = "540.00",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        BigDecimal finalTotalPrice,

        @Schema(
                description = "Currency code (ISO 4217)",
                example = "USD",
                pattern = "^[A-Z]{3}$",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        String currency,

        @Schema(
                description = "Deposit amount required to confirm the reservation",
                example = "108.00",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        BigDecimal depositAmount,

        @Schema(
                description = "Deadline to make the payment",
                example = "2026-05-25",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        LocalDate paymentDeadline,

        @Schema(
                description = "Details of rental price modifications applied",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        RentalPriceModificationResponseDTO priceModification,

        @Schema(
                description = "Breakdown of reservation price per day of the reservation",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        List<ReservationQuoteBreakdownItem> breakdown
) {}
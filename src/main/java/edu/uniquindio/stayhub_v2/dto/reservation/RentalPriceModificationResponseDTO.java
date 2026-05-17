package edu.uniquindio.stayhub_v2.dto.reservation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Summary of how seasonal pricing modified the reservation total compared to the accommodation base nightly rate.")
public record RentalPriceModificationResponseDTO(

        @NotNull
        @Schema(description = "Whether the reservation became cheaper, more expensive, or stayed unchanged after applying seasonal pricing.",
                example = "SAVED",
                allowableValues = {"SAVED", "INCREASED", "UNCHANGED"})
        RentalPriceModificationType type,

        @NotNull
        @Schema(description = "Absolute difference between the base total and the final seasonal total.",
                example = "60000.00")
        BigDecimal amount,

        @NotNull
        @Schema(description = "User-facing message in Spanish that explains the seasonal price effect.",
                example = "Has ahorrado $60.000 COP en esta reserva.")
        String message
) {
}

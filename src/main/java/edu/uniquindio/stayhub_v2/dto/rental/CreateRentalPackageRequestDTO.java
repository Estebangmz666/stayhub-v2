package edu.uniquindio.stayhub_v2.dto.rental;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body used by a HOST to create a new rental package for an accommodation.
 *
 * <p>The package defines a nightly price that replaces the accommodation base
 * rate for a specific date range.</p>
 *
 * @param startDate     First day the package is active (inclusive, must be today or later).
 * @param endDate       Last day the package is active (inclusive, must be after startDate).
 * @param pricePerNight Nightly price that overrides the accommodation base rate.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Request body to create a rental package for an accommodation.")
public record CreateRentalPackageRequestDTO(

        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Start date must be today or in the future")
        @Schema(
                description = "First date (inclusive) the package is active.",
                example = "2026-07-01"
        )
        LocalDate startDate,

        @NotNull(message = "End date is required")
        @Schema(
                description = "Last date (inclusive) the package is active. Must be after startDate.",
                example = "2026-07-31"
        )
        LocalDate endDate,

        @NotNull(message = "Price per night is required")
        @DecimalMin(value = "0.01", message = "Price per night must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price per night must have up to 10 integer digits and 2 decimal places")
        @Schema(
                description = "Nightly price that replaces the accommodation base rate for this period.",
                example = "150000.00"
        )
        BigDecimal pricePerNight
) {
    /**
     * Compact constructor — validates that endDate is strictly after startDate.
     */
    public CreateRentalPackageRequestDTO {
        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }
}

package edu.uniquindio.stayhub_v2.dto.rental;

import edu.uniquindio.stayhub_v2.model.RentalType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body used by a HOST to update an existing rental package.
 *
 * <p>All fields are optional; only non-null values are applied to the existing package.
 * If both {@code startDate} and {@code endDate} are provided, they must form a valid range.</p>
 *
 * @param type      New rental modality (optional).
 * @param startDate New start date (optional, must be today or later if provided).
 * @param endDate   New end date (optional, must be after the effective startDate).
 * @param price     New price (optional, replaces the accommodation's base nightly rate).
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Request body to update a rental package. All fields are optional.")
public record UpdateRentalPackageRequestDTO(

        @Schema(
                description = "New rental modality. Omit to keep the existing value.",
                example = "POR_HABITACIONES",
                nullable = true
        )
        RentalType type,

        @Schema(
                description = "New start date. Must be today or later if provided.",
                example = "2026-08-01",
                nullable = true
        )
        LocalDate startDate,

        @Schema(
                description = "New end date. Must be after startDate if both are provided.",
                example = "2026-08-31",
                nullable = true
        )
        LocalDate endDate,

        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price must have up to 10 integer digits and 2 decimal places")
        @Schema(
                description = "New package price. Omit to keep the existing value.",
                example = "160000.00",
                nullable = true
        )
        BigDecimal price
) {
    /**
     * Compact constructor — validates dates only when both are present.
     */
    public UpdateRentalPackageRequestDTO {
        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }
}

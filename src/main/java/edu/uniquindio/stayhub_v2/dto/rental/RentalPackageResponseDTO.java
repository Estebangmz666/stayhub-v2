package edu.uniquindio.stayhub_v2.dto.rental;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO returned after creating, updating or fetching a rental package.
 *
 * <p>Includes all package fields plus audit timestamps inherited from {@code Auditable}.</p>
 *
 * @param id              Unique identifier of the rental package.
 * @param accommodationId ID of the accommodation this package belongs to.
 * @param startDate       First day the package is active.
 * @param endDate         Last day the package is active.
 * @param pricePerNight   Nightly price for the package period.
 * @param createdAt       Timestamp when the package was created.
 * @param updatedAt       Timestamp of the last update.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Response body representing a rental package.")
public record RentalPackageResponseDTO(

        @Schema(description = "Unique identifier of the rental package.", example = "1")
        Long id,

        @Schema(description = "ID of the accommodation this package belongs to.", example = "15")
        Long accommodationId,

        @Schema(description = "First day the package is active (inclusive).", example = "2026-07-01")
        LocalDate startDate,

        @Schema(description = "Last day the package is active (inclusive).", example = "2026-07-31")
        LocalDate endDate,

        @Schema(description = "Nightly price that replaces the accommodation base rate for this period.", example = "150000.00")
        BigDecimal pricePerNight,

        @Schema(description = "Timestamp when the package was created.", example = "2026-05-04T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "Timestamp of the last modification.", example = "2026-05-04T12:30:00")
        LocalDateTime updatedAt
) {}

package edu.uniquindio.stayhub_v2.dto.host;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Response body returned when a host requests performance metrics for their
 * accommodations within a stay-based period.
 *
 * @param accommodationId               Unique identifier of the accommodation.
 * @param accommodationTitle            Title of the accommodation.
 * @param city                          City where the accommodation is located.
 * @param currency                      Currency code used by the accommodation.
 * @param totalReservationsInPeriod     Total number of overlapping reservations in the requested period.
 * @param activeReservationsInPeriod    Total number of overlapping reservations that remain ACTIVE.
 * @param cancelledReservationsInPeriod Total number of overlapping reservations that are CANCELLED.
 * @param reservedRevenueInPeriod       Total reserved revenue from overlapping reservations.
 * @param paidDepositsCountInPeriod     Number of overlapping reservations whose deposit was paid.
 * @param paidDepositsAmountInPeriod    Total deposit amount already paid for overlapping reservations.
 * @param averageRating                 Average accommodation rating across all reviews.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(
        name = "HostAccommodationMetricsResponse",
        description = "Paginated accommodation performance row returned to the authenticated host."
)
public record HostAccommodationMetricsResponseDTO(

        @Schema(description = "Unique identifier of the accommodation", example = "15")
        Long accommodationId,

        @Schema(description = "Title of the accommodation", example = "Cabana familiar con vista al valle")
        String accommodationTitle,

        @Schema(description = "City where the accommodation is located", example = "Armenia")
        String city,

        @Schema(description = "Currency code used by the accommodation", example = "COP")
        String currency,

        @Schema(description = "Total number of reservations that overlap the requested stay period", example = "8")
        Long totalReservationsInPeriod,

        @Schema(description = "Total number of overlapping reservations with ACTIVE status", example = "5")
        Long activeReservationsInPeriod,

        @Schema(description = "Total number of overlapping reservations with CANCELLED status", example = "3")
        Long cancelledReservationsInPeriod,

        @Schema(description = "Total reserved revenue from overlapping reservations", example = "1440000.00")
        BigDecimal reservedRevenueInPeriod,

        @Schema(description = "Number of overlapping reservations whose deposit was already paid", example = "4")
        Long paidDepositsCountInPeriod,

        @Schema(description = "Total deposit amount already paid for overlapping reservations", example = "288000.00")
        BigDecimal paidDepositsAmountInPeriod,

        @Schema(description = "Average rating of the accommodation across all reviews", example = "4.7", nullable = true)
        Double averageRating
) {}

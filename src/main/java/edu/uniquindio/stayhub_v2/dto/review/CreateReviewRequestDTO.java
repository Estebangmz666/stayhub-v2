package edu.uniquindio.stayhub_v2.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request body used by a guest to create a review for a completed stay.
 *
 * @param reservationId Identifier of the completed reservation that supports
 *                      the review.
 * @param rating        Numeric rating given by the guest.
 * @param comment       Written feedback about the accommodation stay.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Request body used by a guest to create a review for a completed reservation.")
public record CreateReviewRequestDTO(

        @Schema(description = "Identifier of the completed reservation being reviewed", example = "45")
        @NotNull(message = "Reservation ID is required")
        @Positive(message = "Reservation ID must be positive")
        Long reservationId,

        @Schema(description = "Rating given by the guest, from 1 to 5", example = "5")
        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        Integer rating,

        @Schema(description = "Written review content about the stay", example = "Excelente alojamiento, muy limpio y bien ubicado.")
        @NotBlank(message = "Review comment is required")
        @Size(max = 1000, message = "Review comment must not exceed 1000 characters")
        String comment
) {}

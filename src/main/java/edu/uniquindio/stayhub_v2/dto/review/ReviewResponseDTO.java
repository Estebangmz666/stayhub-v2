package edu.uniquindio.stayhub_v2.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Response body returned when a review is created, listed, or answered.
 *
 * @param id              Unique identifier of the review.
 * @param accommodationId Identifier of the reviewed accommodation.
 * @param guestId         Identifier of the guest who wrote the review.
 * @param guestName       Full name of the guest who wrote the review.
 * @param stayStartDate   Check-in date of the completed stay associated with the review.
 * @param stayEndDate     Check-out date of the completed stay associated with the review.
 * @param rating          Numeric rating from 1 to 5.
 * @param comment         Written feedback from the guest.
 * @param hostResponse    Optional response written by the accommodation host.
 * @param hostRespondedAt Date and time when the host response was registered.
 * @param createdAt       Date and time when the review was created.
 * @param updatedAt       Date and time when the review was last updated.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(
        name = "ReviewResponse",
        description = "Response returned when exposing a guest review, including rating, written comment, optional host response, and stay dates."
)
public record ReviewResponseDTO(

        @NotNull
        @Schema(description = "Unique identifier of the review", example = "12")
        Long id,

        @NotNull
        @Schema(description = "Identifier of the reviewed accommodation", example = "7")
        Long accommodationId,

        @NotNull
        @Schema(description = "Identifier of the guest who wrote the review", example = "18")
        Long guestId,

        @NotNull
        @Schema(description = "Full name of the guest who wrote the review", example = "Juan Perez")
        String guestName,

        @NotNull
        @Schema(description = "Check-in date and time of the completed stay", example = "2026-05-10T15:00:00")
        LocalDateTime stayStartDate,

        @NotNull
        @Schema(description = "Check-out date and time of the completed stay", example = "2026-05-14T11:00:00")
        LocalDateTime stayEndDate,

        @NotNull
        @Schema(description = "Rating given by the guest", example = "5", allowableValues = {"1", "2", "3", "4", "5"})
        Integer rating,

        @NotNull
        @Schema(description = "Written review content from the guest", example = "Excelente alojamiento, muy limpio y con una vista hermosa.")
        String comment,

        @Schema(description = "Optional response written by the accommodation host", example = "Muchas gracias por tu visita. Siempre seras bienvenido.")
        String hostResponse,

        @Schema(description = "Date and time when the host response was registered", example = "2026-05-16T09:30:00")
        LocalDateTime hostRespondedAt,

        @NotNull
        @Schema(description = "Date and time when the review was created", example = "2026-05-15T18:00:00")
        LocalDateTime createdAt,

        @NotNull
        @Schema(description = "Date and time when the review was last updated", example = "2026-05-16T09:30:00")
        LocalDateTime updatedAt
) {}

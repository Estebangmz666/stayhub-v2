package edu.uniquindio.stayhub_v2.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body used by a host to answer a guest review.
 *
 * @param response Written response from the host.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Request body used by a host to answer a guest review.")
public record RespondReviewRequestDTO(

        @Schema(description = "Written response from the accommodation host", example = "Gracias por tu visita. Nos alegra saber que disfrutaste la estadia.")
        @NotBlank(message = "Host response is required")
        @Size(max = 1000, message = "Host response must not exceed 1000 characters")
        String response
) {}

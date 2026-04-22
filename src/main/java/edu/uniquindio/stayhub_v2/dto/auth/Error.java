package edu.uniquindio.stayhub_v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for providing standardized error responses.
 * This DTO is used to return a descriptive message and an HTTP status code for API errors.
 *
 * @param message A human-readable message describing the error.
 * @param code The HTTP status code of the error.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Standardized error response returned when the API cannot process a request successfully.")
public record Error(
    @Schema(description = "A message describing the error", example = "The email is already in use")
     String message,

    @Schema(description = "The HTTP status code of the error", example = "400")
     int code
    ){}

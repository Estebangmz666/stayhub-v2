package edu.uniquindio.stayhub_v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for providing standardized error responses.
 * This DTO is used to return a descriptive message and an HTTP status code for API errors.
 * @author Esteban Gómez León
 * @version 1.0
 */
@Schema(description = "Data Transfer Object for providing standardized error responses. This DTO is used to return a descriptive message and an HTTP status code for API errors.")
public class Error {
    /**
     * A human-readable message describing the error.
     */
    @Schema(description = "A message describing the error", example = "The email is already in use")
    private String message;

    /**
     * The HTTP status code of the error.
     */
    @Schema(description = "The HTTP status code of the error", example = "400")
    private int code;
}
package edu.uniquindio.stayhub_v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object used to return a simple operation result message.
 *
 * @param message Human-readable message describing the result of the operation.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Generic response body that returns a human-readable message.")
public record MessageResponseDTO(
        @Schema(description = "Human-readable operation result message", example = "Operation completed successfully")
        String message
) {}

package edu.uniquindio.stayhub_v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object for JWT token response.
 * @author Esteban Gómez León
 * @version 1.0
 */
@Schema(description = "Data Transfer Object for JWT token response") @AllArgsConstructor
public class TokenResponseDTO {
    /**
     * The JWT token.
     */
    @Schema(description = "The JWT token", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiZXhwIjoxNzI1ODk4MDAwfQ.abc")
    private String token;
}
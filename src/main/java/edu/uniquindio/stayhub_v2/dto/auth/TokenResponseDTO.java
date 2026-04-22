package edu.uniquindio.stayhub_v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for JWT token response.
 *
 * @param token JWT access token issued after a successful authentication.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Response body returned after successful authentication with a JWT access token.")
public record TokenResponseDTO(
    @Schema(description = "The JWT token", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiZXhwIjoxNzI1ODk4MDAwfQ.abc")
    String token){}

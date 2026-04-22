package edu.uniquindio.stayhub_v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object used to start the password recovery flow.
 *
 * @param email Email address of the account that requested password recovery.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Request body used to send a password recovery code to a user's email.")
public record ForgotPasswordRequestDTO(
        @Schema(description = "Email address registered in StayHub", example = "john.doe@example.com")
        @NotBlank(message = "El email no puede estar vacío")
        @Email(message = "Debe proporcionar un email válido")
        String email
) {
}

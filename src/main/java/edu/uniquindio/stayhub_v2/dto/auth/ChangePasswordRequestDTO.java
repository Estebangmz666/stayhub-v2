package edu.uniquindio.stayhub_v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object used to request a password change for an authenticated
 * user.
 *
 * @param currentPassword Current password used to verify the user's identity.
 * @param newPassword     New password that must satisfy the platform password
 *                        policy.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Request body used by an authenticated user to change their password.")
public record ChangePasswordRequestDTO(
        @Schema(description = "Current password of the authenticated user", example = "OldPassword123!")
        @NotBlank(message = "La contraseña actual no puede estar vacía")
        String currentPassword,

        @Schema(
                description = "New password that must include at least 8 characters, one uppercase letter, one lowercase letter, one number, and one special character",
                example = "NewPassword123!"
        )
        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Pattern(
                regexp = "^(?=.*\\d)(?=.*\\p{Ll})(?=.*\\p{Lu})(?=.*[^\\p{L}\\d\\s]).{8,}$",
                message = "La contraseña debe tener mínimo 8 caracteres, mayúscula, minúscula, número y carácter especial"
        )
        String newPassword
) {
}

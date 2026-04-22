package edu.uniquindio.stayhub_v2.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object used to complete the password recovery flow.
 *
 * @param email       Email address of the account that requested password
 *                    recovery.
 * @param code        Recovery code sent to the user's email address.
 * @param newPassword New password that must satisfy the platform password
 *                    policy.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Request body used to reset a user's password with a recovery code.")
public record ResetPasswordRequestDTO(
        @Schema(description = "Email address registered in StayHub", example = "john.doe@example.com")
        @NotBlank(message = "El email no puede estar vacío")
        @Email(message = "Debe proporcionar un email válido")
        String email,

        @Schema(description = "Password recovery code sent to the user's email", example = "123456")
        @NotBlank(message = "El código no puede estar vacío")
        String code,

        @Schema(
                description = "New password that must include at least 8 characters, one uppercase letter, one lowercase letter, one number, and one special character",
                example = "NewPassword123!"
        )
        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\\\S+$).{8,}$",
                message = "La contraseña debe tener mínimo 8 caracteres, una letra mayúscula, una letra minúscula, un número y un carácter especial"
        )
        String newPassword
) {
}

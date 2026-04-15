package edu.uniquindio.stayhub_v2.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequestDTO(
        @NotBlank(message = "La contraseña actual no puede estar vacía")
        String currentPassword,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Pattern(
                regexp = "^(?=.*\\d)(?=.*\\p{Ll})(?=.*\\p{Lu})(?=.*[^\\p{L}\\d\\s]).{8,}$",
                message = "La contraseña debe tener mínimo 8 caracteres, mayúscula, minúscula, número y carácter especial"
        )
        String newPassword
) {
}

package edu.uniquindio.stayhub_v2.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequestDTO(
        @NotBlank(message = "La contraseña actual no puede estar vacía")
        String currentPassword,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\\\S+$).{8,}$",
                message = "La contraseña debe tener mínimo 8 caracteres, una letra mayúscula, una letra minúscula, un número y un carácter especial"
        )
        String newPassword
) {
}

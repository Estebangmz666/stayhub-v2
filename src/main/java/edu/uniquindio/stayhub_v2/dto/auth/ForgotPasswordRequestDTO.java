package edu.uniquindio.stayhub_v2.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequestDTO(
        @NotBlank(message = "El email no puede estar vacío")
        @Email(message = "Debe proporcionar un email válido")
        String email
) {
}

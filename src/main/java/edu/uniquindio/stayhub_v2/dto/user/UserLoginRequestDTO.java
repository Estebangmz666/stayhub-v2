package edu.uniquindio.stayhub_v2.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for user login.
 * This DTO contains all the necessary information to log in a user, including their email and password.
 * @param email The user's email address.
 * @param password The user's password.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Request body used to authenticate a user with email and password.")
public record UserLoginRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(description = "The user's email address", example = "john.doe@example.com")
        String email,

        @NotBlank(message = "Password is required")
        @Schema(description = "The user's password", example = "P@ssw0rd123")
        String password
){}

package edu.uniquindio.stayhub_v2.dto.user;

import edu.uniquindio.stayhub_v2.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.util.Set;

/**
 * Data Transfer Object for user registration.
 * This DTO contains all the necessary information to create a new user account.
 *
 * @param email The user's email address. It must be unique and in a valid format.
 * @param password The user's password. It must meet complexity requirements: at least 8 characters, including an uppercase letter and a number.
 * @param roles The role or roles of the user
 * @param fullName The user's full name.
 * @param phoneNumber The user's phone number in Colombian format.
 * @param birthDate The user's birthdate.
 * @param profilePicture The user's profile picture URL.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Request body used to register a new StayHub user account.")
public record UserSignupRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        @Schema(description = "The user's email address. It must be unique and in a valid format", example = "john.doe@example.com")
        String email,

        @NotBlank(message = "Password is required")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$", message = "Password must be at least 8 characters with uppercase and numbers")
        @Schema(description = "The user's password", example = "P@ssw0rd123")
        String password,

        @NotNull(message = "Role is required")
        @Schema(description = "The role or roles assigned to the user", example = "[\"GUEST\"]", allowableValues = {"GUEST", "HOST"})
        Set<Role> roles,

        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must not exceed 100 characters")
        @Schema(description = "The user's full name", example = "John Giggity Doe")
        String fullName,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+57\\s?\\d{10}$", message = "Phone number must follow Colombian format: +57 followed by 10 digits")
        @Schema(description = "The user's phone number in Colombian format", example = "+573101234567")
        String phoneNumber,

        @NotNull(message = "Birthdate is required")
        @Past(message = "Birthdate must be in the past")
        @Schema(description = "The user's birthdate", example = "1990-01-01")
        LocalDate birthDate,

        @URL(message = "Profile picture must be a valid URL")
        @Schema(description = "The user's profile picture URL", example = "https://example.com/profile.jpg")
        String profilePicture
) {}

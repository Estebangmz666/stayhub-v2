package edu.uniquindio.stayhub_v2.dto.user;

import edu.uniquindio.stayhub_v2.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Set;

/**
 * Response body returned when retrieving the authenticated user's profile.
 *
 * @param email email address of the authenticated user
 * @param roles role set assigned to the authenticated user
 * @param fullName full name configured in the user profile
 * @param phoneNumber phone number configured in the user profile
 * @param birthDate birthdate registered for the user
 * @param profilePicture public URL of the user's profile picture
 */
@Schema(description = "Response body returned when retrieving the authenticated user's profile.")
public record UserProfileResponseDTO(
        @Schema(description = "Email address of the authenticated user", example = "john.doe@example.com")
        String email,

        @Schema(description = "Role or roles assigned to the authenticated user", example = "[\"GUEST\"]", allowableValues = {"GUEST", "HOST"})
        Set<Role> roles,

        @Schema(description = "Full name configured in the user profile", example = "John Giggity Doe")
        String fullName,

        @Schema(description = "Phone number configured in the user profile", example = "+573101234567")
        String phoneNumber,

        @Schema(description = "Birth date registered for the user", example = "1990-01-01")
        LocalDate birthDate,

        @Schema(description = "Public URL of the user's profile picture", example = "https://example.com/profile.jpg")
        String profilePicture
) {}

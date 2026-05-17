package edu.uniquindio.stayhub_v2.dto.user.profileUpdate;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(name = "UserProfileUpdateResponse", description = "Response with the updated user profile")
public record UserProfileUpdateResponseDTO(

        @Schema(description = "Unique user ID", example = "42")
        Long id,

        @Schema(description = "Full name of the user", example = "Carlos Pérez")
        String fullName,

        @Schema(description = "Phone number", example = "+573001234567")
        String phoneNumber,

        @Schema(description = "Date of birth", example = "1995-08-15")
        LocalDate birthDate,

        @Schema(description = "Profile picture URL", example = "https://cdn.example.com/users/42/avatar.jpg")
        String profilePicture

) {}
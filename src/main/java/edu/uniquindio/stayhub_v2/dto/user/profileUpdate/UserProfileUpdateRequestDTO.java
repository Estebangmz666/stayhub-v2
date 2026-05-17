package edu.uniquindio.stayhub_v2.dto.user.profileUpdate;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserProfileUpdateRequestDTO(

        @Size(max = 100, message = "Full name must not exceed 100 characters")
        String fullName,

        @Size(max = 30, message = "Phone number must not exceed 30 characters")
        String phoneNumber,

        @PastOrPresent(message = "Birthdate cannot be in the future")
        LocalDate birthDate,

        @Size(max = 500, message = "Profile picture URL must not exceed 500 characters")
        String profilePicture
) {}
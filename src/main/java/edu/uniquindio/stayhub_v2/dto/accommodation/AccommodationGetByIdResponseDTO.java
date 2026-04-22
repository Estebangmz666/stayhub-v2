package edu.uniquindio.stayhub_v2.dto.accommodation;

import edu.uniquindio.stayhub_v2.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object for accommodation details.
 * This DTO contains information about an accommodation, such as its host, name, description, and other relevant details.
 *
 * @param host The user who owns the accommodation.
 * @param title The title of the accommodation.
 * @param description A brief description of the accommodation.
 * @param capacity The maximum number of guests allowed.
 * @param pricePerNight The price per night for the accommodation.
 * @param mainImage The URL of the main image for the accommodation.
 * @param locationDescription A description of the location, such as neighborhood or landmarks.
 * @param city The city where the accommodation is located.
 * @param images A list of URLs for additional images of the accommodation.
 * @param available Indicates whether the accommodation is currently available for booking.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Detailed response body returned when retrieving an accommodation by its identifier.")
public record AccommodationGetByIdResponseDTO (
        @NotNull(message = "Host cannot be null")
        @Schema(description = "The user who owns the accommodation")
        User host,

        @NotBlank(message = "Title cannot be blank")
        @Size(max = 100, message = "Title cannot exceed 100 characters")
        @Schema(description = "The title of the accommodation", example = "Beachfront Villa with Pool")
        String title,

        @NotBlank(message = "Description cannot be blank")
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        @Schema(description = "A detailed description of the accommodation", example = "Spacious villa with private pool and direct beach access.")
        String description,

        @NotNull(message = "Capacity cannot be null")
        @Positive(message = "Capacity must be a positive number")
        @Schema(description = "The maximum number of guests allowed", example = "6")
        Integer capacity,

        @NotNull(message = "Price per night cannot be null")
        @Positive(message = "Price per night must be a positive number")
        @Schema(description = "The price per night for the accommodation", example = "250000.00")
        BigDecimal pricePerNight,

        @URL(message = "Main image must be a valid URL")
        @Schema(description = "The URL of the main image for the accommodation", example = "https://images.example.com/accommodations/main/beachfront-villa.jpg")
        String mainImage,

        @NotBlank(message = "Location description cannot be blank")
        @Size(max = 100, message = "Location description cannot exceed 100 characters")
        @Schema(description = "A description of the location, such as neighborhood or landmarks", example = "Near the main beach and local restaurants")
        String locationDescription,

        @NotBlank(message = "City cannot be blank")
        @Size(max = 100, message = "City cannot exceed 100 characters")
        @Schema(description = "The city where the accommodation is located", example = "Cartagena")
        String city,

        @NotNull(message = "Images cannot be null")
        @Schema(description = "A list of URLs for additional images of the accommodation", example = "[\"https://images.example.com/accommodations/gallery/villa-1.jpg\"]")
        List<@URL String> images,

        @NotNull(message = "Available cannot be null")
        @Schema(description = "Indicates whether the accommodation is currently available for booking", example = "true")
        boolean available

        //List<Comments> comments
) {}

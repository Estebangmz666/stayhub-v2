package edu.uniquindio.stayhub_v2.dto.accommodation;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request body used by hosts to register a new accommodation in StayHub.
 *
 * @param title Listing title visible in search results and accommodation detail views.
 * @param description Detailed public description of the accommodation.
 * @param capacity Maximum number of guests allowed in the accommodation.
 * @param currency Three-letter ISO 4217 currency code used to price the accommodation.
 * @param pricePerNight Standard nightly rate charged for reservations.
 * @param mainImage Public URL of the cover image shown as the primary listing photo.
 * @param longitude Longitude coordinate of the accommodation location.
 * @param latitude Latitude coordinate of the accommodation location.
 * @param locationDescription Human-readable description of the area or exact reference point.
 * @param city City where the accommodation is located.
 * @param images Gallery image URLs associated with the accommodation.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(
        description = "Request body used by an authenticated host to create a new accommodation listing."
)
public record CreateAccommodationRequestDTO(

        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must not exceed 100 characters")
        @Schema(
                description = "Commercial title of the accommodation shown in search results and detail pages.",
                example = "Cabana familiar con vista al valle"
        )
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        @Schema(
                description = "Detailed public description including amenities, environment and value proposition of the listing.",
                example = "Cabana campestre equipada para familias, con cocina integral, zona BBQ y vista panoramica al valle."
        )
        String description,

        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        @Max(value = 50, message = "Capacity must be at most 50")
        @Schema(
                description = "Maximum number of guests that can stay in the accommodation at the same time.",
                example = "6",
                minimum = "1",
                maximum = "50"
        )
        Integer capacity,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must contain exactly 3 characters")
        @Schema(
                description = "ISO 4217 currency code used for the nightly rate of the accommodation.",
                example = "COP"
        )
        String currency,

        @NotNull(message = "Price per night is required")
        @DecimalMin(value = "0.01", message = "Price per night must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price per night must contain up to 10 integer digits and 2 decimal places")
        @Schema(
                description = "Standard nightly price charged for bookings before discounts or special promotions.",
                example = "180000.00"
        )
        BigDecimal pricePerNight,

        @NotBlank(message = "Main image is required")
        @URL(message = "Main image must be a valid URL")
        @Schema(
                description = "Public URL of the cover image displayed as the primary visual representation of the accommodation.",
                example = "https://images.example.com/accommodations/main/cabana-valle.jpg"
        )
        String mainImage,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180")
        @Schema(
                description = "Longitude coordinate of the accommodation location.",
                example = "-75.6811",
                minimum = "-180",
                maximum = "180"
        )
        Double longitude,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90")
        @Schema(
                description = "Latitude coordinate of the accommodation location.",
                example = "4.5339",
                minimum = "-90",
                maximum = "90"
        )
        Double latitude,

        @NotBlank(message = "Location description is required")
        @Size(max = 100, message = "Location description must not exceed 100 characters")
        @Schema(
                description = "Human-readable explanation of the location, nearby references, neighborhood or access points.",
                example = "A 10 minutos del Parque del Cafe, sobre via principal pavimentada."
        )
        String locationDescription,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must not exceed 100 characters")
        @Schema(
                description = "City where the accommodation is physically located.",
                example = "Armenia"
        )
        String city,

        @NotEmpty(message = "Images are required")
        @ArraySchema(
                schema = @Schema(
                        description = "Additional gallery image URL associated with the accommodation.",
                        example = "https://images.example.com/accommodations/gallery/cabana-valle-sala.jpg"
                ),
                minItems = 1
        )
        List<@NotBlank(message = "Each image is required") @URL(message = "Each image must be a valid URL") String> images
) {

    /**
     * Performs additional domain validation not covered by annotations.
     */
    public CreateAccommodationRequestDTO {
        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }

        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
    }
}

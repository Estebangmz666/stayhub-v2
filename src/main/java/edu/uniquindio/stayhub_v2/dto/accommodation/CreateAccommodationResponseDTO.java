package edu.uniquindio.stayhub_v2.dto.accommodation;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response body returned after successfully creating a new accommodation.
 *
 * @param id Unique identifier generated for the new accommodation.
 * @param hostId Unique identifier of the host that owns the accommodation.
 * @param hostEmail Email address of the authenticated host that created the accommodation.
 * @param title Listing title persisted for the accommodation.
 * @param description Detailed description persisted for the accommodation.
 * @param capacity Maximum number of guests accepted by the accommodation.
 * @param currency Currency code used by the accommodation pricing configuration.
 * @param pricePerNight Standard nightly rate configured for the accommodation.
 * @param mainImage Public URL of the primary cover image.
 * @param longitude Longitude coordinate stored for the accommodation.
 * @param latitude Latitude coordinate stored for the accommodation.
 * @param locationDescription Human-readable location reference stored for the accommodation.
 * @param city City stored for search and display purposes.
 * @param images Additional gallery images stored for the accommodation.
 * @param available Availability flag after creation.
 * @param createdAt Timestamp when the accommodation was created.
 * @param updatedAt Timestamp of the latest persistence update, which matches creation time on initial insert.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(
        description = "Response body returned when a host successfully creates a new accommodation listing."
)
public record CreateAccommodationResponseDTO(

        @NotNull
        @Schema(description = "Unique identifier generated for the accommodation.", example = "15")
        Long id,

        @NotNull
        @Schema(description = "Unique identifier of the host owner of the accommodation.", example = "8")
        Long hostId,

        @NotBlank
        @Schema(description = "Email address of the authenticated host that created the listing.", example = "host@example.com")
        String hostEmail,

        @NotBlank
        @Schema(description = "Persisted title of the accommodation listing.", example = "Cabana familiar con vista al valle")
        String title,

        @NotBlank
        @Schema(description = "Persisted public description of the accommodation.", example = "Cabana campestre equipada para familias, con cocina integral, zona BBQ y vista panoramica al valle.")
        String description,

        @NotNull
        @Schema(description = "Maximum supported guest capacity of the accommodation.", example = "6")
        Integer capacity,

        @NotBlank
        @Schema(description = "ISO 4217 currency code used by the nightly rate.", example = "COP")
        String currency,

        @NotNull
        @Schema(description = "Nightly base rate configured for the accommodation.", example = "180000.00")
        BigDecimal pricePerNight,

        @NotBlank
        @Schema(description = "Cover image URL of the accommodation.", example = "https://images.example.com/accommodations/main/cabana-valle.jpg")
        String mainImage,

        @NotNull
        @Schema(description = "Longitude coordinate persisted for the accommodation.", example = "-75.6811")
        Double longitude,

        @NotNull
        @Schema(description = "Latitude coordinate persisted for the accommodation.", example = "4.5339")
        Double latitude,

        @NotBlank
        @Schema(description = "Stored human-readable location description.", example = "A 10 minutos del Parque del Cafe, sobre via principal pavimentada.")
        String locationDescription,

        @NotBlank
        @Schema(description = "Stored city value used by accommodation search.", example = "Armenia")
        String city,

        @NotNull
        @ArraySchema(
                schema = @Schema(
                        description = "Gallery image URL associated with the accommodation.",
                        example = "https://images.example.com/accommodations/gallery/cabana-valle-sala.jpg"
                )
        )
        List<String> images,

        @NotNull
        @Schema(description = "Availability status of the accommodation immediately after creation.", example = "true")
        Boolean available,

        @NotNull
        @Schema(description = "Timestamp when the accommodation was created.", example = "2026-04-28T20:45:00")
        LocalDateTime createdAt,

        @NotNull
        @Schema(description = "Timestamp of the latest accommodation update.", example = "2026-04-28T20:45:00")
        LocalDateTime updatedAt
) {
}

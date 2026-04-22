package edu.uniquindio.stayhub_v2.dto.accommodation;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Basic accommodation data returned by city search")
public record AccommodationSearchByCityResponseDTO(
        @Schema(description = "Accommodation code", example = "1")
        Long accommodationCode,

        @Schema(description = "Accommodation title", example = "Cabana familiar en Armenia")
        String title,

        @Schema(description = "City where the accommodation is located", example = "Armenia")
        String city,

        @Schema(description = "Maximum guest capacity", example = "5")
        Integer capacity,

        @Schema(description = "Nightly price", example = "180000.00")
        BigDecimal pricePerNight,

        @Schema(description = "Currency code", example = "COP")
        String currency,

        @Schema(description = "Main accommodation image URL", example = "https://images.example.com/accommodations/main/cabana-armenia.jpg")
        String mainImage
) {}
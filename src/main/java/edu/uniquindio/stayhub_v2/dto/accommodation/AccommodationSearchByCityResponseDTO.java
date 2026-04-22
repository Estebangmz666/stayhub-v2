package edu.uniquindio.stayhub_v2.dto.accommodation;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Data Transfer Object used to return compact accommodation information in city
 * search results.
 *
 * @param accommodationCode Unique identifier of the accommodation.
 * @param title             Title of the accommodation.
 * @param city              City where the accommodation is located.
 * @param capacity          Maximum number of guests allowed.
 * @param pricePerNight     Nightly price for the accommodation.
 * @param currency          Currency in which the nightly price is expressed.
 * @param mainImage         Main image URL for the accommodation.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Compact accommodation response returned by city search.")
public record AccommodationSearchByCityResponseDTO(
        @Schema(description = "Unique identifier of the accommodation", example = "1")
        Long accommodationCode,

        @Schema(description = "Accommodation title", example = "Cabana familiar en Armenia")
        String title,

        @Schema(description = "City where the accommodation is located", example = "Armenia")
        String city,

        @Schema(description = "Maximum guest capacity", example = "5")
        Integer capacity,

        @Schema(description = "Nightly price for the accommodation", example = "180000.00")
        BigDecimal pricePerNight,

        @Schema(description = "Currency code", example = "COP")
        String currency,

        @Schema(description = "Main accommodation image URL", example = "https://images.example.com/accommodations/main/cabana-armenia.jpg")
        String mainImage
) {}

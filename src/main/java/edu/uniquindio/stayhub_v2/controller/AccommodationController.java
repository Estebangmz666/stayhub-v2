package edu.uniquindio.stayhub_v2.controller;

import edu.uniquindio.stayhub_v2.dto.accommodation.AccommodationGetByIdResponseDTO;
import edu.uniquindio.stayhub_v2.dto.accommodation.AccommodationSearchByCityResponseDTO;
import edu.uniquindio.stayhub_v2.dto.auth.MessageResponseDTO;
import edu.uniquindio.stayhub_v2.service.AccommodationService;
import edu.uniquindio.stayhub_v2.service.JWTService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Accommodation management", description = "Endpoints for managing accommodations")
@RestController
@RequestMapping("/api/v2/accommodations")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AccommodationController {

    private final AccommodationService accommodationService;
    private final JWTService jwtService;

    @Operation(
            summary = "Search accommodations by city",
            description = "Returns active and available rural houses matching the requested city."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accommodations retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid city, page or size parameter")
    })
    @GetMapping
    public ResponseEntity<Page<AccommodationSearchByCityResponseDTO>> searchAccommodationsByCity(
            @RequestParam
            @NotBlank(message = "City is required")
            @Parameter(description = "City name", example = "Armenia", required = true)
            String city,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be zero or greater")
            @Parameter(description = "Zero-based page number", example = "0")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Size must be at least 1")
            @Max(value = 50, message = "Size must be at most 50")
            @Parameter(description = "Page size", example = "10")
            int size) {

        log.info("Searching accommodations by city '{}', page {}, size {}", city, page, size);
        Page<AccommodationSearchByCityResponseDTO> response =
                accommodationService.searchAccommodationsByCity(city, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get accommodation by ID", description = "Retrieves an accommodation by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accommodation retrieved successfully",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AccommodationGetByIdResponseDTO.class),
                        examples = @ExampleObject(
                                name = "Accommodation retrieved successfully",
                                value = "{\\\"host\\\": {\\\"id\\\": 1, \\\"name\\\": \\\"Carlos Ramírez\\\", \\\"email\\\": \\\"carlos.ramirez@email.com\\\"}, \\\"title\\\": \\\"Acogedor apartamento en el centro histórico\\\", \\\"description\\\": \\\"Hermoso apartamento completamente amoblado con vista a la plaza principal.\\\", \\\"capacity\\\": 3, \\\"pricePerNight\\\": 120000.00, \\\"mainImage\\\": \\\"https://images.example.com/accommodations/main/apt-centro-001.jpg\\\", \\\"locationDescription\\\": \\\"A dos cuadras del parque principal, cerca de restaurantes y tiendas\\\", \\\"city\\\": \\\"Armenia\\\", \\\"images\\\": [\\\"https://images.example.com/accommodations/gallery/apt-centro-001-sala.jpg\\\", \\\"https://images.example.com/accommodations/gallery/apt-centro-001-cocina.jpg\\\", \\\"https://images.example.com/accommodations/gallery/apt-centro-001-habitacion.jpg\\\"], \\\"available\\\": true}"
                        )
                )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<AccommodationGetByIdResponseDTO> getAccommodation(
            @PathVariable @Parameter(description = "Accommodation ID", required = true) Long id) {
        log.info("Retrieving accommodation with ID: {}", id);
        AccommodationGetByIdResponseDTO response = accommodationService.getAccommodation(id);
        log.debug("Accommodation retrieved successfully with title: {}", response.title());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Deactivate accommodation", description = "Soft deletes an accommodation if it has no future active reservations, validating if user is the owner.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accommodation deactivated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{\\\"message\\\": \\\"Alojamiento dado de baja con éxito.\\\"}")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Active future reservations exist",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Unauthorized action, user is not the owner",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Accommodation not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class)
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> deactivateAccommodation(
            @PathVariable @Parameter(description = "Accommodation ID", required = true) Long id,
            @RequestHeader("Authorization") String token) {
        log.info("Request to deactivate accommodation with ID: {}", id);
        String requesterEmail = jwtService.getEmailFromToken(token);
        accommodationService.deactivateAccommodation(id, requesterEmail);
        return new ResponseEntity<>(new MessageResponseDTO("Alojamiento dado de baja con éxito."), HttpStatus.OK);
    }
}

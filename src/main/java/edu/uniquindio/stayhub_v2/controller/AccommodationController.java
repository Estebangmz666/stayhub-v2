package edu.uniquindio.stayhub_v2.controller;

import edu.uniquindio.stayhub_v2.dto.accommodation.AccommodationGetByIdResponseDTO;
import edu.uniquindio.stayhub_v2.service.AccommodationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Accommodation management", description = "Endpoints for managing accommodations")
@RestController
@RequestMapping("/api/v2/accommodations")
@RequiredArgsConstructor
@Slf4j
public class AccommodationController {

    private final AccommodationService accommodationService;

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
}
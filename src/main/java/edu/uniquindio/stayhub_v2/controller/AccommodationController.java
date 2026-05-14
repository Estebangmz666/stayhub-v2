package edu.uniquindio.stayhub_v2.controller;

import edu.uniquindio.stayhub_v2.dto.accommodation.AccommodationGetByIdResponseDTO;
import edu.uniquindio.stayhub_v2.dto.accommodation.AccommodationSearchByCityResponseDTO;
import edu.uniquindio.stayhub_v2.dto.accommodation.CreateAccommodationRequestDTO;
import edu.uniquindio.stayhub_v2.dto.accommodation.CreateAccommodationResponseDTO;
import edu.uniquindio.stayhub_v2.dto.accommodation.UnavailableAccommodationDateRangeResponseDTO;
import edu.uniquindio.stayhub_v2.dto.auth.MessageResponseDTO;
import edu.uniquindio.stayhub_v2.service.AccommodationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @Operation(
            summary = "Create accommodation",
            description = "Creates a new accommodation listing owned by the authenticated host. The backend assigns the host automatically from the authenticated user and initializes the listing as available and not deleted."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Accommodation created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateAccommodationResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Accommodation created",
                                    value = "{\\\"id\\\":15,\\\"hostId\\\":8,\\\"hostEmail\\\":\\\"host@example.com\\\",\\\"title\\\":\\\"Cabana familiar con vista al valle\\\",\\\"description\\\":\\\"Cabana campestre equipada para familias, con cocina integral, zona BBQ y vista panoramica al valle.\\\",\\\"capacity\\\":6,\\\"currency\\\":\\\"COP\\\",\\\"pricePerNight\\\":180000.00,\\\"mainImage\\\":\\\"https://images.example.com/accommodations/main/cabana-valle.jpg\\\",\\\"longitude\\\":-75.6811,\\\"latitude\\\":4.5339,\\\"locationDescription\\\":\\\"A 10 minutos del Parque del Cafe, sobre via principal pavimentada.\\\",\\\"city\\\":\\\"Armenia\\\",\\\"images\\\":[\\\"https://images.example.com/accommodations/gallery/cabana-valle-sala.jpg\\\",\\\"https://images.example.com/accommodations/gallery/cabana-valle-habitacion.jpg\\\"],\\\"available\\\":true,\\\"createdAt\\\":\\\"2026-04-28T20:45:00\\\",\\\"updatedAt\\\":\\\"2026-04-28T20:45:00\\\"}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Validation error or malformed request body"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to create accommodations"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, valid JWT token required")
    })
    @PostMapping
    public ResponseEntity<CreateAccommodationResponseDTO> createAccommodation(
            @Valid @RequestBody CreateAccommodationRequestDTO requestDTO) {
        log.info("Creating accommodation with title '{}'", requestDTO.title());
        CreateAccommodationResponseDTO response = accommodationService.createAccommodation(requestDTO);
        log.info("Accommodation created successfully with ID: {}", response.id());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Search accommodations by city",
            description = "Returns active and available rural houses matching the requested city."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accommodations retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid city, page or size parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, valid JWT token required")
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

        log.info(
                "Found {} accommodations for city '{}' on page {} with size {}",
                response.getNumberOfElements(),
                city,
                page,
                size
        );

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
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized, valid JWT token required",
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
    @GetMapping("/{id}")
    public ResponseEntity<AccommodationGetByIdResponseDTO> getAccommodation(
            @PathVariable @Parameter(description = "Accommodation ID", required = true) Long id) {
        log.info("Retrieving accommodation with ID: {}", id);
        AccommodationGetByIdResponseDTO response = accommodationService.getAccommodation(id);
        log.debug("Accommodation retrieved successfully with title: {}", response.title());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Get unavailable accommodation dates",
            description = "Returns a paginated list of blocked date ranges for the given accommodation. " +
                    "Only ACTIVE reservations are included because those are the intervals that currently block new bookings."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unavailable dates retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid accommodation ID, page or size parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, valid JWT token required"),
            @ApiResponse(responseCode = "404", description = "Accommodation not found")
    })
    @GetMapping("/{id}/unavailable-dates")
    public ResponseEntity<Page<UnavailableAccommodationDateRangeResponseDTO>> getUnavailableDateRanges(
            @PathVariable
            @Min(value = 1, message = "Accommodation ID must be positive")
            @Parameter(description = "Accommodation ID", required = true, example = "15")
            Long id,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be zero or greater")
            @Parameter(description = "Zero-based page number", example = "0")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Size must be at least 1")
            @Max(value = 50, message = "Size must be at most 50")
            @Parameter(description = "Page size", example = "10")
            int size) {

        log.info("Retrieving unavailable dates for accommodation {} page {} size {}", id, page, size);
        Page<UnavailableAccommodationDateRangeResponseDTO> response =
                accommodationService.getUnavailableDateRanges(id, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Deactivate accommodation",
            description = """
                Soft deletes an accommodation by its ID.
                The authenticated user must be the owner of the accommodation.
                The accommodation cannot be deactivated if it has future active reservations.
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Accommodation deactivated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Accommodation deactivated",
                                    value = "{\"message\": \"Alojamiento dado de baja con éxito.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The accommodation has future active reservations",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class),
                            examples = @ExampleObject(
                                    name = "Active future reservations",
                                    value = "{\"message\": \"You cannot cancel a house with future reservations\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized. A valid JWT token is required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden. The authenticated user is not the owner of the accommodation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class),
                            examples = @ExampleObject(
                                    name = "Not accommodation owner",
                                    value = "{\"message\": \"You do not have permissions to deactivate this rural house.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Accommodation not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class),
                            examples = @ExampleObject(
                                    name = "Accommodation not found",
                                    value = "{\"message\": \"Accommodation not found with id: 100\"}"
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> deactivateAccommodation(
            @PathVariable @Parameter(description = "Accommodation ID", required = true) Long id) {
        log.info("Request to deactivate accommodation with ID: {}", id);
        accommodationService.deactivateAccommodation(id);
        return new ResponseEntity<>(new MessageResponseDTO("Accommodation successfully deleted."), HttpStatus.OK);
    }
}

package edu.uniquindio.stayhub_v2.controller;

import edu.uniquindio.stayhub_v2.dto.auth.MessageResponseDTO;
import edu.uniquindio.stayhub_v2.dto.rental.CreateRentalPackageRequestDTO;
import edu.uniquindio.stayhub_v2.dto.rental.RentalPackageResponseDTO;
import edu.uniquindio.stayhub_v2.dto.rental.UpdateRentalPackageRequestDTO;
import edu.uniquindio.stayhub_v2.service.RentalPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for the Rental Packages CRUD.
 *
 * <p>Base path: {@code /api/v2/accommodations/{accommodationId}/packages}</p>
 *
 * <p>All write operations (POST, PUT, DELETE) require the authenticated user to
 * have the {@code HOST} role <em>and</em> to be the owner of the accommodation.
 * The GET operation is available to any authenticated user.</p>
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Rental Packages", description = "CRUD of rental packages per accommodation")
@RestController
@RequestMapping("/api/v2/accommodations/{accommodationId}/packages")
@RequiredArgsConstructor
@Slf4j
public class RentalPackageController {

    private final RentalPackageService rentalPackageService;

    // -------------------------------------------------------------------------
    // POST — Create
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Create rental package",
            description = """
                    Creates a seasonal pricing package for the specified accommodation.

                    **Business rules:**
                    - Caller must have the HOST role and be the owner of the accommodation.
                    - The package date range must not overlap with any existing package for
                      the same accommodation.
                    - The `pricePerNight` replaces the accommodation's base `pricePerNight`
                      for every reservation night that falls inside the package range.
                    - Stays that are partially inside and partially outside the package are priced
                      with a mixed calculation night by night.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Package created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RentalPackageResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid date range",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized, valid JWT required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not the HOST owner of this accommodation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class))),
            @ApiResponse(responseCode = "404", description = "Accommodation not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class))),
            @ApiResponse(responseCode = "409", description = "Date range overlaps with an existing package",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class)))
    })
    @PostMapping
    public ResponseEntity<RentalPackageResponseDTO> createPackage(
            @PathVariable
            @Parameter(description = "Accommodation identifier", required = true)
            Long accommodationId,

            @Valid @RequestBody CreateRentalPackageRequestDTO requestDTO) {

        log.info("Creating rental package for accommodation {}", accommodationId);
        RentalPackageResponseDTO response = rentalPackageService.createPackage(accommodationId, requestDTO);
        log.info("Rental package {} created for accommodation {}", response.id(), accommodationId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // -------------------------------------------------------------------------
    // GET — List
    // -------------------------------------------------------------------------

    @Operation(
            summary = "List rental packages",
            description = "Returns all seasonal pricing packages for the given accommodation ordered by start date. "
                    + "Available to any authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Packages retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = RentalPackageResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized, valid JWT required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class))),
            @ApiResponse(responseCode = "404", description = "Accommodation not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class)))
    })
    @GetMapping
    public ResponseEntity<List<RentalPackageResponseDTO>> getPackages(
            @PathVariable
            @Parameter(description = "Accommodation identifier", required = true)
            Long accommodationId) {

        log.info("Fetching rental packages for accommodation {}", accommodationId);
        List<RentalPackageResponseDTO> response =
                rentalPackageService.getPackagesByAccommodation(accommodationId);
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // PUT — Update
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Update rental package",
            description = """
                    Partially updates a seasonal pricing package. Only non-null fields are applied.

                    **Business rules:**
                    - Caller must have the HOST role and be the owner of the accommodation.
                    - If dates are changed the new range must not overlap any other package
                      belonging to the same accommodation.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Package updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RentalPackageResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid date range",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized, valid JWT required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not the HOST owner of this accommodation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class))),
            @ApiResponse(responseCode = "404", description = "Accommodation or package not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class))),
            @ApiResponse(responseCode = "409", description = "New date range overlaps with an existing package",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class)))
    })
    @PutMapping("/{packageId}")
    public ResponseEntity<RentalPackageResponseDTO> updatePackage(
            @PathVariable
            @Parameter(description = "Accommodation identifier", required = true)
            Long accommodationId,

            @PathVariable
            @Parameter(description = "Rental package identifier", required = true)
            Long packageId,

            @Valid @RequestBody UpdateRentalPackageRequestDTO requestDTO) {

        log.info("Updating rental package {} for accommodation {}", packageId, accommodationId);
        RentalPackageResponseDTO response =
                rentalPackageService.updatePackage(accommodationId, packageId, requestDTO);
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // DELETE — Remove
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Delete rental package",
            description = """
                    Permanently deletes a seasonal pricing package.

                    **Business rules:**
                    - Caller must have the HOST role and be the owner of the accommodation.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Package deleted successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized, valid JWT required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not the HOST owner of this accommodation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class))),
            @ApiResponse(responseCode = "404", description = "Accommodation or package not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class)))
    })
    @DeleteMapping("/{packageId}")
    public ResponseEntity<MessageResponseDTO> deletePackage(
            @PathVariable
            @Parameter(description = "Accommodation identifier", required = true)
            Long accommodationId,

            @PathVariable
            @Parameter(description = "Rental package identifier", required = true)
            Long packageId) {

        log.info("Deleting rental package {} from accommodation {}", packageId, accommodationId);
        rentalPackageService.deletePackage(accommodationId, packageId);
        return ResponseEntity.ok(new MessageResponseDTO("Rental package successfully deleted."));
    }
}

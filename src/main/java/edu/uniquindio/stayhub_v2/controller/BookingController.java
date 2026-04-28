package edu.uniquindio.stayhub_v2.controller;

import edu.uniquindio.stayhub_v2.dto.auth.Error;
import edu.uniquindio.stayhub_v2.dto.reservation.CreateReservationRequestDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.CreateReservationResponseDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.RetrieveReservationResponseDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.RetrieveReservationSummaryResponseDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.UpdateReservationRequestDTO;
import edu.uniquindio.stayhub_v2.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Booking management", description = "Endpoints for managing bookings")
@RestController
@RequestMapping("/api/v2/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final ReservationService reservationService;


    @Operation(
            summary = "Create a new accommodation reservation",
            description = "Creates a new reservation for an accommodation based on the provided details. Validates that dates are in the future and end date is after start date."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Reservation successfully created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateReservationResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Reservation created successfully",
                                    summary = "Example of a successful reservation creation response",
                                    value = """
                        {
                            "id": 12345,
                            "startDate": "2025-06-01T14:00:00",
                            "endDate": "2025-06-05T11:00:00",
                            "totalPrice": 450.00,
                            "currency": "USD",
                            "status": "ACTIVE",
                            "accommodationId": 1,
                            "accommodationTitle": "Beachfront Villa with Pool",
                            "userId": 678
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - Validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Error.class),
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    summary = "Example when validation fails",
                                    value = """
                        {
                            "message": "End date must be after start date",
                            "code": 400
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Accommodation not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Error.class),
                            examples = @ExampleObject(
                                    name = "Accommodation Not Found",
                                    summary = "Example when accommodation doesn't exist",
                                    value = """
                        {
                            "code": 404,
                            "message": "Accommodation with ID 999 not found"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Accommodation not available for selected dates",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Error.class),
                            examples = @ExampleObject(
                                    name = "Dates Unavailable",
                                    summary = "Example when accommodation is already booked",
                                    value = """
                        {
                            "code": 409,
                            "message": "Accommodation is not available for the selected dates"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Error.class),
                            examples = @ExampleObject(
                                    name = "Server Error",
                                    summary = "Example of an unexpected server error",
                                    value = """
                        {
                            "code": 500,
                            "message": "An unexpected error occurred while processing your request"
                        }
                        """
                            )
                    )
            )
    })
    @PostMapping("/book")
    public ResponseEntity<CreateReservationResponseDTO> bookAccommodation(@Valid @RequestBody CreateReservationRequestDTO createReservationRequestDTO) {
        log.info("Processing booking request for accommodation ID: {}", createReservationRequestDTO.accommodationId());
        CreateReservationResponseDTO createReservationResponseDTO = reservationService.createReservation(createReservationRequestDTO);
        log.debug("Booking created successfully for accommodation ID: {}", createReservationRequestDTO.accommodationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createReservationResponseDTO);
    }

    @Operation(
            summary = "Get reservation detail by ID",
            description = "Returns the full detail of a reservation. " +
                    "Only the guest who made the reservation or the host of the accommodation can access it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation found and returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation ID"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @GetMapping("/{reservationId}")
    public ResponseEntity<RetrieveReservationResponseDTO> getReservationById(
            @PathVariable @Positive(message = "ID must be positive") Long reservationId) {

        log.info("GET /bookings/{} - retrieving reservation detail", reservationId);
        RetrieveReservationResponseDTO response = reservationService.getReservationById(reservationId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get paginated list of my reservations",
            description = "Returns a paginated list of reservations for the authenticated user. " +
                    "Without scope, keeps legacy behavior: if the user is a HOST, " +
                    "returns reservations for all their accommodations; otherwise returns " +
                    "their reservations as GUEST. " +
                    "Optional scope can be host, guest, or all. " +
                    "Results are sorted by start date descending, 10 per page."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservations retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid page or scope parameter"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @GetMapping("/my-reservations")
    public ResponseEntity<Page<RetrieveReservationSummaryResponseDTO>> getMyReservations(
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Optional filter: host, guest, all")
            @RequestParam(required = false) String scope) {

        log.info("GET /bookings/my-reservations?page={}&scope={} - retrieving reservations list",
                page, scope);
        Page<RetrieveReservationSummaryResponseDTO> response = reservationService.getMyReservations(page, scope);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update an active reservation date range",
            description = "Allows the guest who owns an active reservation to update its check-in and check-out dates. " +
                    "If the new check-in is within 72 hours, the reservation deposit must already be paid."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or reservation policy violation"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Only the guest can update the reservation"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "409", description = "Dates unavailable or deposit not paid")
    })
    @PutMapping("/{reservationId}")
    public ResponseEntity<RetrieveReservationResponseDTO> updateReservation(
            @PathVariable @Positive(message = "ID must be positive") Long reservationId,
            @Valid @RequestBody UpdateReservationRequestDTO updateReservationRequestDTO) {

        log.info("PUT /bookings/{} - updating reservation dates", reservationId);
        RetrieveReservationResponseDTO response = reservationService.updateReservation(
                reservationId,
                updateReservationRequestDTO
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Cancel an active reservation",
            description = "Cancels an active reservation owned by the authenticated guest. " +
                    "Cancellation is allowed only at least 48 hours before check-in."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Reservation cannot be cancelled by policy"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Only the guest can cancel the reservation"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<RetrieveReservationResponseDTO> cancelReservation(
            @PathVariable @Positive(message = "ID must be positive") Long reservationId) {

        log.info("PATCH /bookings/{}/cancel - cancelling reservation", reservationId);
        RetrieveReservationResponseDTO response = reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Mark reservation deposit as paid",
            description = "Marks the advance deposit of an authorized reservation as paid."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deposit marked as paid successfully"),
            @ApiResponse(responseCode = "400", description = "Reservation is not active"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @PatchMapping("/{reservationId}/deposit-paid")
    public ResponseEntity<RetrieveReservationResponseDTO> markDepositAsPaid(
            @PathVariable @Positive(message = "ID must be positive") Long reservationId) {

        log.info("PATCH /bookings/{}/deposit-paid - marking deposit as paid", reservationId);
        RetrieveReservationResponseDTO response = reservationService.markDepositAsPaid(reservationId);
        return ResponseEntity.ok(response);
    }
}

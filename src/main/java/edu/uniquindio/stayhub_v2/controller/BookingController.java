package edu.uniquindio.stayhub_v2.controller;

import edu.uniquindio.stayhub_v2.dto.reservation.CreateReservationRequestDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.CreateReservationResponseDTO;
import edu.uniquindio.stayhub_v2.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    summary = "Example when validation fails",
                                    value = """
                        {
                            "timestamp": "2025-04-10T10:30:00",
                            "status": 400,
                            "errors": [
                                "End date must be after start date",
                                "Start date must be in the future"
                            ]
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
                            examples = @ExampleObject(
                                    name = "Accommodation Not Found",
                                    summary = "Example when accommodation doesn't exist",
                                    value = """
                        {
                            "timestamp": "2025-04-10T10:30:00",
                            "status": 404,
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
                            examples = @ExampleObject(
                                    name = "Dates Unavailable",
                                    summary = "Example when accommodation is already booked",
                                    value = """
                        {
                            "timestamp": "2025-04-10T10:30:00",
                            "status": 409,
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
                            examples = @ExampleObject(
                                    name = "Server Error",
                                    summary = "Example of an unexpected server error",
                                    value = """
                        {
                            "timestamp": "2025-04-10T10:30:00",
                            "status": 500,
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
}
package edu.uniquindio.stayhub_v2.controller;

import edu.uniquindio.stayhub_v2.dto.auth.Error;
import edu.uniquindio.stayhub_v2.dto.review.CreateReviewRequestDTO;
import edu.uniquindio.stayhub_v2.dto.review.RespondReviewRequestDTO;
import edu.uniquindio.stayhub_v2.dto.review.ReviewResponseDTO;
import edu.uniquindio.stayhub_v2.service.ReviewService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Review management", description = "Endpoints for managing guest reviews and host responses")
@RestController
@RequestMapping("/api/v2")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
            summary = "Create a review for a completed stay",
            description = "Allows the guest who completed a reservation to create a single review for the related accommodation."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review created successfully"),
            @ApiResponse(responseCode = "400", description = "Review policy violation or invalid request",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized, valid JWT token required",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Only the guest owner can create the review",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Accommodation or reservation not found",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    @PostMapping("/accommodations/{accommodationId}/reviews")
    public ResponseEntity<ReviewResponseDTO> createReview(
            @PathVariable
            @Positive(message = "Accommodation ID must be positive")
            @Parameter(description = "Accommodation ID", required = true)
            Long accommodationId,
            @Valid @RequestBody CreateReviewRequestDTO createReviewRequestDTO) {

        log.info("POST /accommodations/{}/reviews - creating review", accommodationId);
        ReviewResponseDTO response = reviewService.createReview(accommodationId, createReviewRequestDTO);
        log.info("Review created successfully with ID: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "List reviews for an accommodation",
            description = "Returns all reviews registered for the specified accommodation ordered from newest to oldest."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, valid JWT token required",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Accommodation not found",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    @GetMapping("/accommodations/{accommodationId}/reviews")
    public ResponseEntity<List<ReviewResponseDTO>> getAccommodationReviews(
            @PathVariable
            @Positive(message = "Accommodation ID must be positive")
            @Parameter(description = "Accommodation ID", required = true)
            Long accommodationId) {

        log.info("GET /accommodations/{}/reviews - retrieving accommodation reviews", accommodationId);
        return ResponseEntity.ok(reviewService.getAccommodationReviews(accommodationId));
    }

    @Operation(
            summary = "Answer a guest review",
            description = "Allows the host owner of the accommodation to respond once to a guest review."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review answered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReviewResponseDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 12,
                                              "accommodationId": 7,
                                              "guestId": 18,
                                              "guestName": "Juan Perez",
                                              "stayStartDate": "2026-05-10T15:00:00",
                                              "stayEndDate": "2026-05-14T11:00:00",
                                              "rating": 5,
                                              "comment": "Excelente alojamiento, muy limpio y con una vista hermosa.",
                                              "hostResponse": "Muchas gracias por tu visita. Siempre seras bienvenido.",
                                              "hostRespondedAt": "2026-05-16T09:30:00",
                                              "createdAt": "2026-05-15T18:00:00",
                                              "updatedAt": "2026-05-16T09:30:00"
                                            }
                                            """
                            )
                    )),
            @ApiResponse(responseCode = "400", description = "Review policy violation or invalid request",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized, valid JWT token required",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Only the host owner can answer the review",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Review not found",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    @PostMapping("/reviews/{reviewId}/response")
    public ResponseEntity<ReviewResponseDTO> respondToReview(
            @PathVariable
            @Positive(message = "Review ID must be positive")
            @Parameter(description = "Review ID", required = true)
            Long reviewId,
            @Valid @RequestBody RespondReviewRequestDTO respondReviewRequestDTO) {

        log.info("POST /reviews/{}/response - answering review", reviewId);
        ReviewResponseDTO response = reviewService.respondToReview(reviewId, respondReviewRequestDTO);
        log.info("Review answered successfully with ID: {}", response.id());
        return ResponseEntity.ok(response);
    }
}
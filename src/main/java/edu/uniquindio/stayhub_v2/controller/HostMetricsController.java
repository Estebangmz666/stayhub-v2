package edu.uniquindio.stayhub_v2.controller;

import edu.uniquindio.stayhub_v2.dto.host.HostAccommodationMetricsResponseDTO;
import edu.uniquindio.stayhub_v2.service.HostMetricsService;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Controller that exposes host-facing accommodation metrics endpoints.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Host metrics", description = "Endpoints for host performance analytics by accommodation.")
@RestController
@RequestMapping("/api/v2/hosts/me/accommodations")
@RequiredArgsConstructor
@Slf4j
@Validated
public class HostMetricsController {

    private final HostMetricsService hostMetricsService;

    @Operation(
            summary = "Get accommodation metrics by stay period",
            description = "Returns paginated performance metrics for accommodations owned by the authenticated host. " +
                    "The requested period is evaluated using reservation stay overlap, not reservation creation time."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Host accommodation metrics retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HostAccommodationMetricsPageSchema.class),
                            examples = @ExampleObject(
                                    name = "Metrics page",
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "accommodationId": 15,
                                                  "accommodationTitle": "Cabana familiar con vista al valle",
                                                  "city": "Armenia",
                                                  "currency": "COP",
                                                  "totalReservationsInPeriod": 8,
                                                  "activeReservationsInPeriod": 5,
                                                  "cancelledReservationsInPeriod": 3,
                                                  "reservedRevenueInPeriod": 1440000.00,
                                                  "paidDepositsCountInPeriod": 4,
                                                  "paidDepositsAmountInPeriod": 288000.00,
                                                  "averageRating": 4.7
                                                }
                                              ],
                                              "totalElements": 1,
                                              "totalPages": 1,
                                              "size": 10,
                                              "number": 0,
                                              "numberOfElements": 1,
                                              "first": true,
                                              "last": true,
                                              "empty": false
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid date range, page or size parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, valid JWT token required"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have HOST role")
    })
    @GetMapping("/metrics")
    public ResponseEntity<Page<HostAccommodationMetricsResponseDTO>> getAccommodationMetricsByPeriod(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Inclusive start date of the requested stay period", example = "2026-05-01", required = true)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Inclusive end date of the requested stay period", example = "2026-05-31", required = true)
            LocalDate endDate,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be zero or greater")
            @Parameter(description = "Zero-based page number", example = "0")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Size must be at least 1")
            @Max(value = 50, message = "Size must be at most 50")
            @Parameter(description = "Page size", example = "10")
            int size) {

        log.info("Retrieving host accommodation metrics from {} to {} page {} size {}",
                startDate, endDate, page, size);

        Page<HostAccommodationMetricsResponseDTO> response =
                hostMetricsService.getAccommodationMetricsByPeriod(startDate, endDate, page, size);

        return ResponseEntity.ok(response);
    }

    @Schema(name = "HostAccommodationMetricsPage")
    private static final class HostAccommodationMetricsPageSchema {
    }
}

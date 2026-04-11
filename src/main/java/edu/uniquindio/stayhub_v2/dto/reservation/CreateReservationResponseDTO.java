package edu.uniquindio.stayhub_v2.dto.reservation;

import edu.uniquindio.stayhub_v2.model.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

/**
 * Data Transfer Object representing the response for the creation of a reservation.
 * This record encapsulates all the details related to a newly created reservation.
 *
 * @param id The unique identifier of the reservation.
 * @param startDate The start date and time of the reservation.
 * @param endDate The end date and time of the reservation.
 * @param totalPrice The total price calculated for the reservation.
 * @param currency The currency in which the total price is expressed.
 * @param status The current status of the reservation, such as ACTIVE, CANCELLED, or COMPLETED.
 * @param accommodationId The unique identifier of the associated accommodation.
 * @param accommodationTitle The title or name of the associated accommodation.
 * @param userId The unique identifier of the user who made the reservation.
 *
 * @author Esteban Gómez León
 * @version 1.0
 */
@Schema(description = "Data Transfer Object representing the response for the creation of a reservation.")
public record CreateReservationResponseDTO(

        @NotNull
        Long id,

        @NotNull
        LocalDateTime startDate,

        @NotNull
        LocalDateTime endDate,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal totalPrice,

        @NotNull
        Currency currency,

        @NotNull
        ReservationStatus status,

        @NotNull
        Long accommodationId,

        @NotNull
        String accommodationTitle,

        @NotNull
        Long userId
) {}
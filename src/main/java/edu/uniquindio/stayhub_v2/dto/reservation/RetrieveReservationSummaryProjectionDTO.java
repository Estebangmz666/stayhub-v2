package edu.uniquindio.stayhub_v2.dto.reservation;

import edu.uniquindio.stayhub_v2.model.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

/**
 * Internal projection used to retrieve compact reservation summary data from
 * repository queries.
 *
 * <p>
 * This record is not exposed directly by controllers. It keeps custom JPQL
 * constructor expressions aligned with the public summary response DTO.
 * </p>
 *
 * @param id                 Unique identifier of the reservation.
 * @param accommodationId    Unique identifier of the booked accommodation.
 * @param accommodationTitle Title of the booked accommodation.
 * @param startDate          Check-in date and time of the reservation.
 * @param endDate            Check-out date and time of the reservation.
 * @param totalPrice         Total price calculated for the reservation.
 * @param currency           Currency in which the total price is expressed.
 * @param status             Current status of the reservation.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
public record RetrieveReservationSummaryProjectionDTO(
        Long id,
        Long accommodationId,
        String accommodationTitle,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BigDecimal totalPrice,
        Currency currency,
        ReservationStatus status
) {
}

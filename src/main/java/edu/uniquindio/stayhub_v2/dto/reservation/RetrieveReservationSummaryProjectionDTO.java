package edu.uniquindio.stayhub_v2.dto.reservation;

import edu.uniquindio.stayhub_v2.model.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

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

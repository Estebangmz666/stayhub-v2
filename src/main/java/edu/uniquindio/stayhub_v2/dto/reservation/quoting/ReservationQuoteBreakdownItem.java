package edu.uniquindio.stayhub_v2.dto.reservation.quoting;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservationQuoteBreakdownItem(
        LocalDate date,
        BigDecimal nightPrice,
        SourceType source
) {}
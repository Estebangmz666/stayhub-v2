package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.dto.reservation.RentalPriceModificationResponseDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.quoting.ReservationQuoteBreakdownItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Internal pricing result used by the reservation pricing engine.
 *
 * <p>This record contains the complete result of a reservation price calculation,
 * including the number of nights, base price, final price, deposit amount,
 * payment deadline, applied rental price modification, and the daily price
 * breakdown.</p>
 *
 * <p>It is used internally by the backend to avoid duplicating pricing logic
 * between reservation quoting and reservation creation flows.</p>
 *
 * @param nights the number of nights included in the reservation
 * @param baseTotalPrice the total price before applying discounts or price modifications
 * @param finalTotalPrice the final total price after applying all price modifications
 * @param depositAmount the deposit amount required to confirm the reservation
 * @param paymentDeadline the deadline for paying the reservation deposit
 * @param rentalPriceModification the rental price modification applied to the calculation, if any
 * @param breakdown the daily breakdown of the calculated reservation price
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
public record ReservationPricingDetails(
        long nights,
        BigDecimal baseTotalPrice,
        BigDecimal finalTotalPrice,
        BigDecimal depositAmount,
        LocalDateTime paymentDeadline,
        RentalPriceModificationResponseDTO rentalPriceModification,
        List<ReservationQuoteBreakdownItem> breakdown
) {}
package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.dto.reservation.quoting.ReservationQuoteRequestDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.quoting.ReservationQuoteResponseDTO;
import edu.uniquindio.stayhub_v2.exception.AccommodationAlreadyBookedException;
import edu.uniquindio.stayhub_v2.exception.AccommodationNotFoundException;
import edu.uniquindio.stayhub_v2.exception.ReservationPolicyViolationException;
import edu.uniquindio.stayhub_v2.model.Accommodation;
import edu.uniquindio.stayhub_v2.repository.AccommodationRepository;
import edu.uniquindio.stayhub_v2.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service responsible for generating reservation quotes without creating or persisting reservations.
 *
 * <p>This service validates the requested reservation dates, checks accommodation availability,
 * verifies that there are no overlapping reservations, delegates the pricing calculation to
 * {@link ReservationService}, and builds a {@link ReservationQuoteResponseDTO} with the complete
 * quote details.</p>
 *
 * <p>The quote operation is read-only and does not modify the database.</p>
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class QuotingService {

    private static final int MINIMUM_BOOKING_ANTICIPATION_HOURS = 72;

    private final AccommodationRepository accommodationRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    /**
     * Generates a reservation quote for the requested accommodation and date range.
     *
     * <p>This method performs the complete quote flow:</p>
     * <ul>
     *   <li>Validates that the reservation will start at least 72 hours in the future</li>
     *   <li>Finds the requested accommodation if it exists, is not deleted, and is available</li>
     *   <li>Checks whether the selected date range overlaps with an existing reservation</li>
     *   <li>Calculates the reservation pricing details using the reservation pricing engine</li>
     *   <li>Builds and returns a quote response with totals, deposit, deadline, and daily breakdown</li>
     * </ul>
     *
     * <p>This method does not persist any reservation. It only simulates the cost of a possible
     * reservation based on the current accommodation pricing rules and availability.</p>
     *
     * @param reservationQuoteRequest the request containing the accommodation ID and reservation date range
     * @return a {@link ReservationQuoteResponseDTO} containing the calculated quote details
     *
     * @throws ReservationPolicyViolationException if the reservation does not meet the minimum anticipation policy
     * @throws AccommodationNotFoundException if the accommodation does not exist, is deleted, or is unavailable
     * @throws AccommodationAlreadyBookedException if the accommodation is already booked for the selected date range
     * @throws ArithmeticException if the calculated number of nights cannot be safely converted to an {@code int}
     */
    @Transactional(readOnly = true)
    public ReservationQuoteResponseDTO quoteReservation(
            ReservationQuoteRequestDTO reservationQuoteRequest) {

        validateMinimumBookingAnticipation(reservationQuoteRequest.startDate());

        Accommodation accommodation = accommodationRepository
                .findByIdAndDeletedFalse(reservationQuoteRequest.accommodationId())
                .filter(Accommodation::isAvailable)
                .orElseThrow(() -> new AccommodationNotFoundException(
                        "Accommodation not found or unavailable"
                ));

        boolean isOverlapping = reservationRepository.existsByAccommodationIdAndDateRange(
                reservationQuoteRequest.accommodationId(),
                reservationQuoteRequest.startDate(),
                reservationQuoteRequest.endDate()
        );

        if (isOverlapping) {
            throw new AccommodationAlreadyBookedException("Accommodation is already booked for the selected dates");
        }

        ReservationPricingDetails pricingDetails = reservationService.calculateReservationPricing(
                accommodation,
                reservationQuoteRequest.startDate(),
                reservationQuoteRequest.endDate()
        );

        return new ReservationQuoteResponseDTO(
                accommodation.getId(),
                reservationQuoteRequest.startDate(),
                reservationQuoteRequest.endDate(),
                Math.toIntExact(pricingDetails.nights()),
                pricingDetails.baseTotalPrice(),
                pricingDetails.finalTotalPrice(),
                accommodation.getCurrency().getCurrencyCode(),
                pricingDetails.depositAmount(),
                pricingDetails.paymentDeadline().toLocalDate(),
                pricingDetails.rentalPriceModification(),
                pricingDetails.breakdown()
        );
    }

    /**
     * Validates that the requested reservation start date complies with the minimum booking anticipation policy.
     *
     * <p>Reservations must be requested at least {@value #MINIMUM_BOOKING_ANTICIPATION_HOURS}
     * hours before the check-in date.</p>
     *
     * @param startDate the requested reservation start date and time
     * @throws ReservationPolicyViolationException if the start date is before the minimum allowed start date
     */
    private void validateMinimumBookingAnticipation(LocalDateTime startDate) {
        LocalDateTime minimumStartDate = LocalDateTime.now()
                .plusHours(MINIMUM_BOOKING_ANTICIPATION_HOURS);

        if (startDate.isBefore(minimumStartDate)) {
            throw new ReservationPolicyViolationException(
                    "Reservations must be created at least 72 hours before check-in"
            );
        }
    }
}

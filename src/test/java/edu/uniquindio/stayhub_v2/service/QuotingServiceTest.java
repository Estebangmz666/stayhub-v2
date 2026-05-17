package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.dto.reservation.RentalPriceModificationResponseDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.RentalPriceModificationType;
import edu.uniquindio.stayhub_v2.dto.reservation.quoting.ReservationQuoteBreakdownItem;
import edu.uniquindio.stayhub_v2.dto.reservation.quoting.ReservationQuoteRequestDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.quoting.ReservationQuoteResponseDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.quoting.SourceType;
import edu.uniquindio.stayhub_v2.exception.AccommodationNotFoundException;
import edu.uniquindio.stayhub_v2.exception.ReservationPolicyViolationException;
import edu.uniquindio.stayhub_v2.model.Accommodation;
import edu.uniquindio.stayhub_v2.repository.AccommodationRepository;
import edu.uniquindio.stayhub_v2.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuotingServiceTest {

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private QuotingService quotingService;

    private Accommodation accommodation;

    @BeforeEach
    void setUp() {
        accommodation = Accommodation.builder()
                .id(10L)
                .title("Cabana en Quindio")
                .pricePerNight(new BigDecimal("200000"))
                .currency(Currency.getInstance("COP"))
                .available(true)
                .build();
    }

    @Test
    void quoteReservation_ValidRequest_ReturnsReservationQuoteResponse() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = start.plusDays(4);
        ReservationQuoteRequestDTO request = new ReservationQuoteRequestDTO(10L, start, end);

        ReservationPricingDetails pricingDetails = new ReservationPricingDetails(
                4,
                new BigDecimal("800000"),
                new BigDecimal("1000000"),
                new BigDecimal("200000.00"),
                LocalDateTime.now().plusDays(3),
                new RentalPriceModificationResponseDTO(
                        RentalPriceModificationType.INCREASED,
                        new BigDecimal("200000"),
                        "El precio de esta reserva aumentó $200.000 COP por tarifa de temporada."
                ),
                List.of(
                        new ReservationQuoteBreakdownItem(start.toLocalDate(), new BigDecimal("200000"), SourceType.BASE),
                        new ReservationQuoteBreakdownItem(start.toLocalDate().plusDays(1), new BigDecimal("300000"), SourceType.SEASONAL),
                        new ReservationQuoteBreakdownItem(start.toLocalDate().plusDays(2), new BigDecimal("300000"), SourceType.SEASONAL),
                        new ReservationQuoteBreakdownItem(start.toLocalDate().plusDays(3), new BigDecimal("200000"), SourceType.BASE)
                )
        );

        when(accommodationRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.existsByAccommodationIdAndDateRange(10L, start, end)).thenReturn(false);
        when(reservationService.calculateReservationPricing(accommodation, start, end)).thenReturn(pricingDetails);

        ReservationQuoteResponseDTO response = quotingService.quoteReservation(request);

        assertThat(response.accommodationId()).isEqualTo(10L);
        assertThat(response.nights()).isEqualTo(4);
        assertThat(response.baseTotalPrice()).isEqualByComparingTo("800000");
        assertThat(response.finalTotalPrice()).isEqualByComparingTo("1000000");
        assertThat(response.depositAmount()).isEqualByComparingTo("200000.00");
        assertThat(response.breakdown()).hasSize(4);
    }

    @Test
    void quoteReservation_AccommodationNotFound_ThrowsException() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = start.plusDays(2);
        ReservationQuoteRequestDTO request = new ReservationQuoteRequestDTO(10L, start, end);

        when(accommodationRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quotingService.quoteReservation(request))
                .isInstanceOf(AccommodationNotFoundException.class);

        verify(reservationService, never()).calculateReservationPricing(any(), any(), any());
    }

    @Test
    void quoteReservation_Within72Hours_ThrowsPolicyViolationException() {
        LocalDateTime start = LocalDateTime.now().plusHours(48);
        LocalDateTime end = start.plusDays(2);
        ReservationQuoteRequestDTO request = new ReservationQuoteRequestDTO(10L, start, end);

        assertThatThrownBy(() -> quotingService.quoteReservation(request))
                .isInstanceOf(ReservationPolicyViolationException.class)
                .hasMessageContaining("72 hours");

        verify(accommodationRepository, never()).findByIdAndDeletedFalse(any());
    }

    @Test
    void quoteReservation_OverlappingDates_ThrowsIllegalStateException() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = start.plusDays(2);
        ReservationQuoteRequestDTO request = new ReservationQuoteRequestDTO(10L, start, end);

        when(accommodationRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.existsByAccommodationIdAndDateRange(eq(10L), eq(start), eq(end))).thenReturn(true);

        assertThatThrownBy(() -> quotingService.quoteReservation(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already booked");

        verify(reservationService, never()).calculateReservationPricing(any(), any(), any());
    }
}

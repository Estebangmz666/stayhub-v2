package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.dto.reservation.CreateReservationRequestDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.CreateReservationResponseDTO;
import edu.uniquindio.stayhub_v2.event.ReservationCreatedEvent;
import edu.uniquindio.stayhub_v2.exception.AccommodationNotFoundException;
import edu.uniquindio.stayhub_v2.mapper.ReservationMapper;
import edu.uniquindio.stayhub_v2.model.Accommodation;
import edu.uniquindio.stayhub_v2.model.Reservation;
import edu.uniquindio.stayhub_v2.model.ReservationStatus;
import edu.uniquindio.stayhub_v2.model.User;
import edu.uniquindio.stayhub_v2.repository.AccommodationRepository;
import edu.uniquindio.stayhub_v2.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ReservationService}.
 *
 * <p>Verifies the core booking flow including payment detail calculation:
 * deposit amount (20%), bank account number and 3-day payment deadline.</p>
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserService userService;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ReservationService reservationService;

    private Accommodation accommodation;
    private User guest;
    private Reservation savedReservation;

    private static final String BANK_ACCOUNT = "3001234567890";
    private static final int DEPOSIT_PERCENTAGE = 20;
    private static final int DEADLINE_DAYS = 3;

    @BeforeEach
    void setUp() {
        // Inject @Value fields via ReflectionTestUtils
        ReflectionTestUtils.setField(reservationService, "bankAccountNumber", BANK_ACCOUNT);
        ReflectionTestUtils.setField(reservationService, "depositPercentage", DEPOSIT_PERCENTAGE);
        ReflectionTestUtils.setField(reservationService, "deadlineDays", DEADLINE_DAYS);

        guest = User.builder()
                .id(1L)
                .email("guest@mail.com")
                .fullName("Juan Pérez")
                .build();

        accommodation = Accommodation.builder()
                .id(10L)
                .title("Cabaña en el Quindío")
                .pricePerNight(new BigDecimal("200000"))
                .currency(Currency.getInstance("COP"))
                .build();

        savedReservation = new Reservation();
        savedReservation.setId(100L);
        savedReservation.setGuest(guest);
        savedReservation.setAccommodation(accommodation);
        savedReservation.setTotalPrice(new BigDecimal("600000"));
        savedReservation.setCurrency(Currency.getInstance("COP"));
        savedReservation.setStatus(ReservationStatus.ACTIVE);
        savedReservation.setDepositAmount(new BigDecimal("120000.00"));
        savedReservation.setPaymentDeadline(LocalDateTime.now().plusDays(3));
        savedReservation.setDepositPaid(false);
        savedReservation.setStartDate(LocalDateTime.now().plusDays(10));
        savedReservation.setEndDate(LocalDateTime.now().plusDays(13));
    }

    // ---------------------------------------------------------------------------
    // Helper: build a base DTO from savedReservation
    // ---------------------------------------------------------------------------
    private CreateReservationResponseDTO baseDto() {
        return new CreateReservationResponseDTO(
                savedReservation.getId(),
                savedReservation.getStartDate(),
                savedReservation.getEndDate(),
                savedReservation.getTotalPrice(),
                savedReservation.getCurrency(),
                savedReservation.getStatus(),
                accommodation.getId(),
                accommodation.getTitle(),
                guest.getId(),
                null, null, null  // payment fields — mapper ignores them
        );
    }

    // ---------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------

    @Test
    void createReservation_ValidRequest_ReturnsResponseWithPaymentDetails() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = LocalDateTime.now().plusDays(13);
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(10L, start, end);

        when(accommodationRepository.findById(10L)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.existsByAccommodationIdAndDateRange(any(), any(), any())).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(reservationMapper.toDTO(savedReservation)).thenReturn(baseDto());

        // Act
        CreateReservationResponseDTO response = reservationService.createReservation(request);

        // Assert — payment fields present
        assertThat(response.depositAmount()).isNotNull();
        assertThat(response.bankAccountNumber()).isEqualTo(BANK_ACCOUNT);
        assertThat(response.paymentDeadline()).isNotNull();

        // Assert — other core fields intact
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.status()).isEqualTo(ReservationStatus.ACTIVE);

        verify(applicationEventPublisher).publishEvent(any(ReservationCreatedEvent.class));
    }

    @Test
    void createReservation_CalculatesCorrect20Percent() {
        // Arrange — accommodation costs 200,000 COP/night, 3 nights = 600,000 total
        //            20% of 600,000 = 120,000
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = LocalDateTime.now().plusDays(13);
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(10L, start, end);

        when(accommodationRepository.findById(10L)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.existsByAccommodationIdAndDateRange(any(), any(), any())).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(reservationMapper.toDTO(savedReservation)).thenReturn(baseDto());

        // Act
        CreateReservationResponseDTO response = reservationService.createReservation(request);

        // Assert — 20% of 600,000 = 120,000.00
        BigDecimal expectedDeposit = new BigDecimal("600000")
                .multiply(BigDecimal.valueOf(20))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        assertThat(response.depositAmount()).isEqualByComparingTo(expectedDeposit);
    }

    @Test
    void createReservation_PaymentDeadlineIs3DaysFromNow() {
        // Arrange
        LocalDateTime before = LocalDateTime.now().plusDays(3).minusSeconds(5);
        LocalDateTime after  = LocalDateTime.now().plusDays(3).plusSeconds(5);

        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = LocalDateTime.now().plusDays(13);
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(10L, start, end);

        when(accommodationRepository.findById(10L)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.existsByAccommodationIdAndDateRange(any(), any(), any())).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(reservationMapper.toDTO(savedReservation)).thenReturn(baseDto());

        // Act
        CreateReservationResponseDTO response = reservationService.createReservation(request);

        // Assert — deadline is within ±5 seconds of now+3days
        assertThat(response.paymentDeadline()).isBetween(before, after);
    }

    @Test
    void createReservation_PersistsDepositAmountAndDeadlineInEntity() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = LocalDateTime.now().plusDays(13);
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(10L, start, end);

        when(accommodationRepository.findById(10L)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.existsByAccommodationIdAndDateRange(any(), any(), any())).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(reservationMapper.toDTO(savedReservation)).thenReturn(baseDto());

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);

        // Act
        reservationService.createReservation(request);

        // Assert — entity persisted with deposit fields set
        verify(reservationRepository).save(captor.capture());
        Reservation persisted = captor.getValue();

        assertThat(persisted.getDepositAmount()).isNotNull();
        assertThat(persisted.getPaymentDeadline()).isNotNull();
        assertThat(persisted.getDepositPaid()).isFalse();
    }

    @Test
    void createReservation_AccommodationNotFound_ThrowsException() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = LocalDateTime.now().plusDays(13);
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(999L, start, end);

        when(accommodationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(AccommodationNotFoundException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_DatesOverlapping_ThrowsIllegalStateException() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = LocalDateTime.now().plusDays(13);
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(10L, start, end);

        when(accommodationRepository.findById(10L)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.existsByAccommodationIdAndDateRange(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already booked");

        verify(reservationRepository, never()).save(any());
    }
}

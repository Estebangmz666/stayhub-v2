package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.dto.reservation.CreateReservationRequestDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.CreateReservationResponseDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.RentalPriceModificationType;
import edu.uniquindio.stayhub_v2.dto.reservation.RetrieveReservationSummaryProjectionDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.RetrieveReservationResponseDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.RetrieveReservationSummaryResponseDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.UpdateReservationRequestDTO;
import edu.uniquindio.stayhub_v2.event.ReservationCancelledEvent;
import edu.uniquindio.stayhub_v2.event.ReservationCompletedEvent;
import edu.uniquindio.stayhub_v2.event.ReservationCreatedEvent;
import edu.uniquindio.stayhub_v2.exception.AccommodationNotFoundException;
import edu.uniquindio.stayhub_v2.exception.DepositNotPaidException;
import edu.uniquindio.stayhub_v2.exception.ReservationNotFoundException;
import edu.uniquindio.stayhub_v2.exception.ReservationPolicyViolationException;
import edu.uniquindio.stayhub_v2.mapper.ReservationMapper;
import edu.uniquindio.stayhub_v2.model.Accommodation;
import edu.uniquindio.stayhub_v2.model.RentalPackage;
import edu.uniquindio.stayhub_v2.model.Role;
import edu.uniquindio.stayhub_v2.model.Reservation;
import edu.uniquindio.stayhub_v2.model.ReservationStatus;
import edu.uniquindio.stayhub_v2.model.User;
import edu.uniquindio.stayhub_v2.repository.AccommodationRepository;
import edu.uniquindio.stayhub_v2.repository.RentalPackageRepository;
import edu.uniquindio.stayhub_v2.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RentalPackageRepository rentalPackageRepository;

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
    private User host;
    private Reservation savedReservation;

    private static final String BANK_ACCOUNT = "3001234567890";
    private static final int DEPOSIT_PERCENTAGE = 20;
    private static final int DEADLINE_DAYS = 3;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reservationService, "bankAccountNumber", BANK_ACCOUNT);
        ReflectionTestUtils.setField(reservationService, "depositPercentage", DEPOSIT_PERCENTAGE);
        ReflectionTestUtils.setField(reservationService, "deadlineDays", DEADLINE_DAYS);

        guest = User.builder()
                .id(1L)
                .email("guest@mail.com")
                .fullName("Juan Perez")
                .roles(Set.of(Role.GUEST))
                .build();

        host = User.builder()
                .id(2L)
                .email("host@mail.com")
                .fullName("Laura Host")
                .roles(Set.of(Role.HOST))
                .build();

        accommodation = Accommodation.builder()
                .id(10L)
                .title("Cabana en Quindio")
                .pricePerNight(new BigDecimal("200000"))
                .currency(Currency.getInstance("COP"))
                .host(host)
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

        lenient().when(rentalPackageRepository.findOverlappingPackagesForStay(any(), any(), any()))
                .thenReturn(List.of());
    }

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
                null, null, null, null
        );
    }

    private RetrieveReservationResponseDTO retrieveDto() {
        return new RetrieveReservationResponseDTO(
                savedReservation.getId(),
                savedReservation.getStartDate(),
                savedReservation.getEndDate(),
                accommodation.getId(),
                accommodation.getTitle(),
                "Armenia",
                guest.getId(),
                guest.getEmail(),
                savedReservation.getTotalPrice(),
                savedReservation.getCurrency().getCurrencyCode(),
                savedReservation.getDepositAmount(),
                savedReservation.getDepositPaid(),
                savedReservation.getPaymentDeadline(),
                savedReservation.getStatus(),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );
    }

    private RetrieveReservationSummaryProjectionDTO summaryProjection() {
        return new RetrieveReservationSummaryProjectionDTO(
                savedReservation.getId(),
                accommodation.getId(),
                accommodation.getTitle(),
                savedReservation.getStartDate(),
                savedReservation.getEndDate(),
                savedReservation.getTotalPrice(),
                savedReservation.getCurrency(),
                savedReservation.getStatus()
        );
    }

    @Test
    void createReservation_ValidRequest_ReturnsResponseWithPaymentDetails() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = LocalDateTime.now().plusDays(13);
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(10L, start, end);

        when(accommodationRepository.findAvailableByIdWithWriteLock(10L)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.existsByAccommodationIdAndDateRange(any(), any(), any())).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(reservationMapper.toDTO(savedReservation)).thenReturn(baseDto());

        CreateReservationResponseDTO response = reservationService.createReservation(request);

        assertThat(response.depositAmount()).isNotNull();
        assertThat(response.bankAccountNumber()).isEqualTo(BANK_ACCOUNT);
        assertThat(response.paymentDeadline()).isNotNull();
        assertThat(response.rentalPriceModification()).isNotNull();
        assertThat(response.rentalPriceModification().type()).isEqualTo(RentalPriceModificationType.UNCHANGED);
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.status()).isEqualTo(ReservationStatus.ACTIVE);

        verify(applicationEventPublisher).publishEvent(any(ReservationCreatedEvent.class));
    }

    @Test
    void createReservation_CalculatesCorrect20Percent() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = LocalDateTime.now().plusDays(13);
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(10L, start, end);

        when(accommodationRepository.findAvailableByIdWithWriteLock(10L)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.existsByAccommodationIdAndDateRange(any(), any(), any())).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(reservationMapper.toDTO(savedReservation)).thenReturn(baseDto());

        CreateReservationResponseDTO response = reservationService.createReservation(request);

        BigDecimal expectedDeposit = new BigDecimal("600000")
                .multiply(BigDecimal.valueOf(20))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        assertThat(response.depositAmount()).isEqualByComparingTo(expectedDeposit);
    }

    @Test
    void createReservation_WithoutSeasonalPricing_ReturnsUnchangedModification() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = LocalDateTime.now().plusDays(13);
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(10L, start, end);

        when(accommodationRepository.findAvailableByIdWithWriteLock(10L)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.existsByAccommodationIdAndDateRange(any(), any(), any())).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(reservationMapper.toDTO(savedReservation)).thenReturn(baseDto());

        CreateReservationResponseDTO response = reservationService.createReservation(request);

        assertThat(response.rentalPriceModification().type()).isEqualTo(RentalPriceModificationType.UNCHANGED);
        assertThat(response.rentalPriceModification().amount()).isEqualByComparingTo("0.00");
        assertThat(response.rentalPriceModification().message())
                .contains("no tuvo cambios de precio por temporada");
    }

    @Test
    void createReservation_AllNightsWithinPackage_UsesPackageNightlyPrice() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = start.plusDays(3);
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(10L, start, end);

        RentalPackage rentalPackage = RentalPackage.builder()
                .id(1L)
                .accommodation(accommodation)
                .startDate(start.toLocalDate())
                .endDate(end.toLocalDate().minusDays(1))
                .pricePerNight(new BigDecimal("300000"))
                .build();

        when(accommodationRepository.findAvailableByIdWithWriteLock(10L)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.existsByAccommodationIdAndDateRange(any(), any(), any())).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(guest);
        when(rentalPackageRepository.findOverlappingPackagesForStay(
                eq(10L),
                eq(start.toLocalDate()),
                eq(end.toLocalDate().minusDays(1))
        )).thenReturn(List.of(rentalPackage));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(reservationMapper.toDTO(savedReservation)).thenReturn(baseDto());

        reservationService.createReservation(request);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        assertThat(captor.getValue().getTotalPrice()).isEqualByComparingTo("900000");
    }

    @Test
    void createReservation_MixedPackageAndBasePricing_SumsNightlySegments() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = start.plusDays(4);
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(10L, start, end);

        RentalPackage rentalPackage = RentalPackage.builder()
                .id(1L)
                .accommodation(accommodation)
                .startDate(start.toLocalDate().plusDays(1))
                .endDate(start.toLocalDate().plusDays(2))
                .pricePerNight(new BigDecimal("300000"))
                .build();

        when(accommodationRepository.findAvailableByIdWithWriteLock(10L)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.existsByAccommodationIdAndDateRange(any(), any(), any())).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(guest);
        when(rentalPackageRepository.findOverlappingPackagesForStay(
                eq(10L),
                eq(start.toLocalDate()),
                eq(end.toLocalDate().minusDays(1))
        )).thenReturn(List.of(rentalPackage));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(reservationMapper.toDTO(savedReservation)).thenReturn(baseDto());

        reservationService.createReservation(request);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        assertThat(captor.getValue().getTotalPrice()).isEqualByComparingTo("1000000");
    }

    @Test
    void createReservation_AllNightsWithinPackage_ReturnsIncreasedModification() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = start.plusDays(3);
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(10L, start, end);

        RentalPackage rentalPackage = RentalPackage.builder()
                .id(1L)
                .accommodation(accommodation)
                .startDate(start.toLocalDate())
                .endDate(end.toLocalDate().minusDays(1))
                .pricePerNight(new BigDecimal("300000"))
                .build();

        when(accommodationRepository.findAvailableByIdWithWriteLock(10L)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.existsByAccommodationIdAndDateRange(any(), any(), any())).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(guest);
        when(rentalPackageRepository.findOverlappingPackagesForStay(
                eq(10L),
                eq(start.toLocalDate()),
                eq(end.toLocalDate().minusDays(1))
        )).thenReturn(List.of(rentalPackage));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(reservationMapper.toDTO(savedReservation)).thenReturn(baseDto());

        CreateReservationResponseDTO response = reservationService.createReservation(request);

        assertThat(response.rentalPriceModification().type()).isEqualTo(RentalPriceModificationType.INCREASED);
        assertThat(response.rentalPriceModification().amount()).isEqualByComparingTo("300000");
        assertThat(response.rentalPriceModification().message()).contains("aumentó");
    }

    @Test
    void createReservation_MixedPricingWithDiscountedPackage_ReturnsSavedModification() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = start.plusDays(4);
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(10L, start, end);

        RentalPackage rentalPackage = RentalPackage.builder()
                .id(1L)
                .accommodation(accommodation)
                .startDate(start.toLocalDate().plusDays(1))
                .endDate(start.toLocalDate().plusDays(2))
                .pricePerNight(new BigDecimal("150000"))
                .build();

        when(accommodationRepository.findAvailableByIdWithWriteLock(10L)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.existsByAccommodationIdAndDateRange(any(), any(), any())).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(guest);
        when(rentalPackageRepository.findOverlappingPackagesForStay(
                eq(10L),
                eq(start.toLocalDate()),
                eq(end.toLocalDate().minusDays(1))
        )).thenReturn(List.of(rentalPackage));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(reservationMapper.toDTO(savedReservation)).thenReturn(baseDto());

        CreateReservationResponseDTO response = reservationService.createReservation(request);

        assertThat(response.rentalPriceModification().type()).isEqualTo(RentalPriceModificationType.SAVED);
        assertThat(response.rentalPriceModification().amount()).isEqualByComparingTo("100000");
        assertThat(response.rentalPriceModification().message()).contains("Has ahorrado");
    }

    @Test
    void createReservation_PaymentDeadlineIs3DaysFromNow() {
        LocalDateTime before = LocalDateTime.now().plusDays(3).minusSeconds(5);
        LocalDateTime after = LocalDateTime.now().plusDays(3).plusSeconds(5);

        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = LocalDateTime.now().plusDays(13);
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(10L, start, end);

        when(accommodationRepository.findAvailableByIdWithWriteLock(10L)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.existsByAccommodationIdAndDateRange(any(), any(), any())).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(reservationMapper.toDTO(savedReservation)).thenReturn(baseDto());

        CreateReservationResponseDTO response = reservationService.createReservation(request);

        assertThat(response.paymentDeadline()).isBetween(before, after);
    }

    @Test
    void createReservation_StartDateWithin72Hours_ThrowsPolicyViolationException() {
        LocalDateTime start = LocalDateTime.now().plusHours(48);
        LocalDateTime end = start.plusDays(2);
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(10L, start, end);

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(ReservationPolicyViolationException.class)
                .hasMessageContaining("72 hours");

        verify(accommodationRepository, never()).findAvailableByIdWithWriteLock(any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_PersistsDepositAmountAndDeadlineInEntity() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = LocalDateTime.now().plusDays(13);
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(10L, start, end);

        when(accommodationRepository.findAvailableByIdWithWriteLock(10L)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.existsByAccommodationIdAndDateRange(any(), any(), any())).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(reservationMapper.toDTO(savedReservation)).thenReturn(baseDto());

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);

        reservationService.createReservation(request);

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

        when(accommodationRepository.findAvailableByIdWithWriteLock(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(AccommodationNotFoundException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_DatesOverlapping_ThrowsIllegalStateException() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = LocalDateTime.now().plusDays(13);
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(10L, start, end);

        when(accommodationRepository.findAvailableByIdWithWriteLock(10L)).thenReturn(Optional.of(accommodation));
        when(reservationRepository.existsByAccommodationIdAndDateRange(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already booked");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void getReservationById_AuthorizedGuest_ReturnsReservation() {
        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.findAuthorizedById(100L, guest.getId()))
                .thenReturn(Optional.of(savedReservation));
        when(reservationMapper.toRetrieveDTO(savedReservation)).thenReturn(retrieveDto());

        RetrieveReservationResponseDTO response = reservationService.getReservationById(100L);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.guestId()).isEqualTo(guest.getId());
        verify(reservationRepository).findAuthorizedById(100L, guest.getId());
    }

    @Test
    void getReservationById_AuthorizedHost_ReturnsReservation() {
        when(userService.getCurrentUser()).thenReturn(host);
        when(reservationRepository.findAuthorizedById(100L, host.getId()))
                .thenReturn(Optional.of(savedReservation));
        when(reservationMapper.toRetrieveDTO(savedReservation)).thenReturn(retrieveDto());

        RetrieveReservationResponseDTO response = reservationService.getReservationById(100L);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.accommodationId()).isEqualTo(accommodation.getId());
        verify(reservationRepository).findAuthorizedById(100L, host.getId());
    }

    @Test
    void getReservationById_NotFoundOrNotAuthorized_ThrowsReservationNotFoundException() {
        User anotherUser = User.builder()
                .id(99L)
                .email("other@mail.com")
                .fullName("Other User")
                .build();

        when(userService.getCurrentUser()).thenReturn(anotherUser);
        when(reservationRepository.findAuthorizedById(100L, anotherUser.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getReservationById(100L))
                .isInstanceOf(ReservationNotFoundException.class)
                .hasMessageContaining("100");

        verify(reservationMapper, never()).toRetrieveDTO(any());
    }

    @Test
    void getMyReservations_DefaultScope_HostUserUsesHostQuery() {
        when(userService.getCurrentUser()).thenReturn(host);
        Page<RetrieveReservationSummaryProjectionDTO> hostPage = new PageImpl<>(List.of(summaryProjection()));
        when(reservationRepository.findSummaryByHostId(eq(host.getId()), any(Pageable.class)))
                .thenReturn(hostPage);

        Page<RetrieveReservationSummaryResponseDTO> response = reservationService.getMyReservations(0);

        assertThat(response.getContent()).hasSize(1);
        verify(reservationRepository).findSummaryByHostId(eq(host.getId()), any(Pageable.class));
        verify(reservationRepository, never()).findSummaryByGuestId(eq(host.getId()), any(Pageable.class));
    }

    @Test
    void getMyReservations_DefaultScope_GuestUserUsesGuestQuery() {
        when(userService.getCurrentUser()).thenReturn(guest);
        Page<RetrieveReservationSummaryProjectionDTO> guestPage = new PageImpl<>(List.of(summaryProjection()));
        when(reservationRepository.findSummaryByGuestId(eq(guest.getId()), any(Pageable.class)))
                .thenReturn(guestPage);

        Page<RetrieveReservationSummaryResponseDTO> response = reservationService.getMyReservations(0);

        assertThat(response.getContent()).hasSize(1);
        verify(reservationRepository).findSummaryByGuestId(eq(guest.getId()), any(Pageable.class));
        verify(reservationRepository, never()).findSummaryByHostId(eq(guest.getId()), any(Pageable.class));
    }

    @Test
    void getMyReservations_ScopeGuest_UsesGuestQuery() {
        User dualRole = User.builder()
                .id(3L)
                .email("dual@mail.com")
                .fullName("Dual User")
                .roles(Set.of(Role.HOST, Role.GUEST))
                .build();

        when(userService.getCurrentUser()).thenReturn(dualRole);
        Page<RetrieveReservationSummaryProjectionDTO> guestPage = new PageImpl<>(List.of(summaryProjection()));
        when(reservationRepository.findSummaryByGuestId(eq(dualRole.getId()), any(Pageable.class)))
                .thenReturn(guestPage);

        Page<RetrieveReservationSummaryResponseDTO> response =
                reservationService.getMyReservations(0, "guest");

        assertThat(response.getContent()).hasSize(1);
        verify(reservationRepository).findSummaryByGuestId(eq(dualRole.getId()), any(Pageable.class));
    }

    @Test
    void getMyReservations_ScopeHost_UsesHostQuery() {
        when(userService.getCurrentUser()).thenReturn(host);
        Page<RetrieveReservationSummaryProjectionDTO> hostPage = new PageImpl<>(List.of(summaryProjection()));
        when(reservationRepository.findSummaryByHostId(eq(host.getId()), any(Pageable.class)))
                .thenReturn(hostPage);

        Page<RetrieveReservationSummaryResponseDTO> response =
                reservationService.getMyReservations(0, "host");

        assertThat(response.getContent()).hasSize(1);
        verify(reservationRepository).findSummaryByHostId(eq(host.getId()), any(Pageable.class));
    }

    @Test
    void getMyReservations_ScopeAll_UsesDistinctCombinedQuery() {
        User dualRole = User.builder()
                .id(3L)
                .email("dual@mail.com")
                .fullName("Dual User")
                .roles(Set.of(Role.HOST, Role.GUEST))
                .build();

        when(userService.getCurrentUser()).thenReturn(dualRole);
        Page<RetrieveReservationSummaryProjectionDTO> allPage = new PageImpl<>(List.of(summaryProjection()));
        when(reservationRepository.findSummaryByGuestOrHostId(
                eq(dualRole.getId()), any(Pageable.class))).thenReturn(allPage);

        Page<RetrieveReservationSummaryResponseDTO> response =
                reservationService.getMyReservations(0, "all");

        assertThat(response.getContent()).hasSize(1);
        verify(reservationRepository).findSummaryByGuestOrHostId(
                eq(dualRole.getId()), any(Pageable.class));
    }

    @Test
    void getMyReservations_InvalidScope_ThrowsIllegalArgumentException() {
        when(userService.getCurrentUser()).thenReturn(guest);

        assertThatThrownBy(() -> reservationService.getMyReservations(0, "invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid scope value");
    }

    @Test
    void updateReservation_ValidDates_RecalculatesPriceAndKeepsDepositPaidState() {
        savedReservation.setDepositPaid(true);
        LocalDateTime newStart = LocalDateTime.now().plusDays(8);
        LocalDateTime newEnd = LocalDateTime.now().plusDays(12);
        UpdateReservationRequestDTO request = new UpdateReservationRequestDTO(newStart, newEnd);

        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.findAuthorizedById(100L, guest.getId()))
                .thenReturn(Optional.of(savedReservation));
        when(reservationRepository.existsByAccommodationIdAndDateRangeExcludingReservationId(
                eq(accommodation.getId()), eq(100L), eq(newStart), eq(newEnd)))
                .thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationMapper.toRetrieveDTO(any(Reservation.class))).thenReturn(retrieveDto());

        RetrieveReservationResponseDTO response = reservationService.updateReservation(100L, request);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(savedReservation.getStartDate()).isEqualTo(newStart);
        assertThat(savedReservation.getEndDate()).isEqualTo(newEnd);
        assertThat(savedReservation.getTotalPrice()).isEqualByComparingTo("800000");
        assertThat(savedReservation.getDepositAmount()).isEqualByComparingTo("160000.00");
        assertThat(savedReservation.getDepositPaid()).isTrue();
    }

    @Test
    void updateReservation_MixedPackageAndBasePricing_RecalculatesWithSeasonalRates() {
        savedReservation.setDepositPaid(true);
        LocalDateTime newStart = LocalDateTime.now().plusDays(8);
        LocalDateTime newEnd = newStart.plusDays(4);
        UpdateReservationRequestDTO request = new UpdateReservationRequestDTO(newStart, newEnd);

        RentalPackage rentalPackage = RentalPackage.builder()
                .id(1L)
                .accommodation(accommodation)
                .startDate(newStart.toLocalDate().plusDays(1))
                .endDate(newStart.toLocalDate().plusDays(2))
                .pricePerNight(new BigDecimal("300000"))
                .build();

        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.findAuthorizedById(100L, guest.getId()))
                .thenReturn(Optional.of(savedReservation));
        when(reservationRepository.existsByAccommodationIdAndDateRangeExcludingReservationId(
                eq(accommodation.getId()), eq(100L), eq(newStart), eq(newEnd)))
                .thenReturn(false);
        when(rentalPackageRepository.findOverlappingPackagesForStay(
                eq(accommodation.getId()),
                eq(newStart.toLocalDate()),
                eq(newEnd.toLocalDate().minusDays(1))
        )).thenReturn(List.of(rentalPackage));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationMapper.toRetrieveDTO(any(Reservation.class))).thenReturn(retrieveDto());

        reservationService.updateReservation(100L, request);

        assertThat(savedReservation.getTotalPrice()).isEqualByComparingTo("1000000");
        assertThat(savedReservation.getDepositAmount()).isEqualByComparingTo("200000.00");
    }

    @Test
    void updateReservation_Within72HoursAndDepositNotPaid_ThrowsDepositNotPaidException() {
        savedReservation.setDepositPaid(false);
        LocalDateTime newStart = LocalDateTime.now().plusHours(48);
        LocalDateTime newEnd = newStart.plusDays(2);
        UpdateReservationRequestDTO request = new UpdateReservationRequestDTO(newStart, newEnd);

        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.findAuthorizedById(100L, guest.getId()))
                .thenReturn(Optional.of(savedReservation));

        assertThatThrownBy(() -> reservationService.updateReservation(100L, request))
                .isInstanceOf(DepositNotPaidException.class)
                .hasMessageContaining("Deposit must be paid");

        verify(reservationRepository, never())
                .existsByAccommodationIdAndDateRangeExcludingReservationId(any(), any(), any(), any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void updateReservation_NonActiveReservation_ThrowsPolicyViolationException() {
        savedReservation.setStatus(ReservationStatus.CANCELLED);
        UpdateReservationRequestDTO request = new UpdateReservationRequestDTO(
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(7)
        );

        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.findAuthorizedById(100L, guest.getId()))
                .thenReturn(Optional.of(savedReservation));

        assertThatThrownBy(() -> reservationService.updateReservation(100L, request))
                .isInstanceOf(ReservationPolicyViolationException.class)
                .hasMessageContaining("Only active reservations");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancelReservation_ValidActiveReservation_CancelsAndPublishesEvent() {
        savedReservation.setStartDate(LocalDateTime.now().plusDays(5));

        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.findAuthorizedById(100L, guest.getId()))
                .thenReturn(Optional.of(savedReservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationMapper.toRetrieveDTO(any(Reservation.class))).thenReturn(retrieveDto());

        RetrieveReservationResponseDTO response = reservationService.cancelReservation(100L);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(savedReservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        verify(applicationEventPublisher).publishEvent(any(ReservationCancelledEvent.class));
    }

    @Test
    void cancelReservation_Within48Hours_ThrowsPolicyViolationException() {
        savedReservation.setStartDate(LocalDateTime.now().plusHours(24));

        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.findAuthorizedById(100L, guest.getId()))
                .thenReturn(Optional.of(savedReservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(100L))
                .isInstanceOf(ReservationPolicyViolationException.class)
                .hasMessageContaining("48 hours");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void markDepositAsPaid_ActiveReservation_SetsDepositPaidTrue() {
        savedReservation.setDepositPaid(false);

        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.findAuthorizedById(100L, guest.getId()))
                .thenReturn(Optional.of(savedReservation));
        when(reservationRepository.markDepositAsPaidForGuest(
                100L,
                guest.getId(),
                ReservationStatus.ACTIVE
        )).thenReturn(1);
        when(reservationMapper.toRetrieveDTO(any(Reservation.class))).thenReturn(retrieveDto());

        reservationService.markDepositAsPaid(100L);

        assertThat(savedReservation.getDepositPaid()).isTrue();
        verify(reservationRepository).markDepositAsPaidForGuest(
                100L,
                guest.getId(),
                ReservationStatus.ACTIVE
        );
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void markDepositAsPaid_HostUser_ThrowsAccessDeniedException() {
        savedReservation.setDepositPaid(false);

        when(userService.getCurrentUser()).thenReturn(host);
        when(reservationRepository.findAuthorizedById(100L, host.getId()))
                .thenReturn(Optional.of(savedReservation));

        assertThatThrownBy(() -> reservationService.markDepositAsPaid(100L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only the guest");

        verify(reservationRepository, never()).markDepositAsPaidForGuest(any(), any(), any());
    }

    @Test
    void cancelExpiredUnpaidReservations_CancelsAllExpiredUnpaidActiveReservations() {
        Reservation expired = new Reservation();
        expired.setId(300L);
        expired.setStatus(ReservationStatus.ACTIVE);
        expired.setDepositPaid(false);

        when(reservationRepository.findExpiredUnpaidReservations(any()))
                .thenReturn(List.of(expired));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int cancelledCount = reservationService.cancelExpiredUnpaidReservations();

        assertThat(cancelledCount).isEqualTo(1);
        assertThat(expired.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        verify(applicationEventPublisher, times(1)).publishEvent(any(ReservationCancelledEvent.class));
    }

    @Test
    void completePastReservations_CompletesReservationsAndPublishesCompletedEvent() {
        Reservation completedCandidate = new Reservation();
        completedCandidate.setId(400L);
        completedCandidate.setStatus(ReservationStatus.ACTIVE);
        completedCandidate.setGuest(guest);
        completedCandidate.setAccommodation(accommodation);
        completedCandidate.setEndDate(LocalDateTime.now().minusDays(1));

        when(reservationRepository.findReservationsToComplete(any()))
                .thenReturn(List.of(completedCandidate));
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        int completedCount = reservationService.completePastReservations();

        assertThat(completedCount).isEqualTo(1);
        assertThat(completedCandidate.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
        verify(applicationEventPublisher).publishEvent(any(ReservationCompletedEvent.class));
    }
}

package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.dto.reservation.CreateReservationRequestDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.CreateReservationResponseDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.RentalPriceModificationResponseDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.RentalPriceModificationType;
import edu.uniquindio.stayhub_v2.dto.reservation.RetrieveReservationSummaryProjectionDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.RetrieveReservationResponseDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.RetrieveReservationSummaryResponseDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.UpdateReservationRequestDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.quoting.ReservationQuoteBreakdownItem;
import edu.uniquindio.stayhub_v2.dto.reservation.quoting.SourceType;
import edu.uniquindio.stayhub_v2.event.ReservationCancelledEvent;
import edu.uniquindio.stayhub_v2.event.ReservationCompletedEvent;
import edu.uniquindio.stayhub_v2.event.ReservationCreatedEvent;
import edu.uniquindio.stayhub_v2.exception.AccommodationNotFoundException;
import edu.uniquindio.stayhub_v2.exception.DepositNotPaidException;
import edu.uniquindio.stayhub_v2.exception.ReservationNotFoundException;
import edu.uniquindio.stayhub_v2.exception.ReservationPolicyViolationException;
import edu.uniquindio.stayhub_v2.mapper.ReservationMapper;
import edu.uniquindio.stayhub_v2.model.*;
import edu.uniquindio.stayhub_v2.repository.AccommodationRepository;
import edu.uniquindio.stayhub_v2.repository.RentalPackageRepository;
import edu.uniquindio.stayhub_v2.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

/**
 * Service class for managing reservation (booking) operations.
 *
 * <p>This service handles all business logic related to creating, managing,
 * and validating reservations. It coordinates between accommodations, users,
 * and reservations to ensure data integrity and enforce business rules.</p>
 *
 * <p><b>Core Responsibilities:</b></p>
 * <ul>
 *   <li>Validating accommodation availability for requested dates</li>
 *   <li>Calculating total reservation price based on nights</li>
 *   <li>Creating and persisting reservations</li>
 *   <li>Publishing domain events for asynchronous processing</li>
 *   <li>Enforcing minimum stay requirements</li>
 *   <li>Preventing double bookings through overlap detection</li>
 * </ul>
 *
 * <p><b>Business Rules Enforced:</b></p>
 * <ul>
 *   <li>Reservation must be for at least one night (end date after start date)</li>
 *   <li>Cannot book accommodation that overlaps with existing active reservations</li>
 *   <li>Total price calculated as {@code pricePerNight × numberOfNights}</li>
 *   <li>Reservations are created with {@link ReservationStatus#ACTIVE} status</li>
 *   <li>Currency is inherited from the accommodation</li>
 * </ul>
 *
 * <p><b>Event-Driven Architecture:</b></p>
 * After a reservation is successfully created and persisted, a
 * {@link ReservationCreatedEvent} is published. This event triggers
 * asynchronous processes such as:
 * <ul>
 *   <li>Sending confirmation emails to guest and host</li>
 *   <li>Updating availability calendars</li>
 *   <li>Logging analytics data</li>
 *   <li>Triggering third-party integrations</li>
 * </ul>
 *
 * <p><b>Transactional Boundaries:</b></p>
 * The entire reservation creation process is wrapped in a single transaction
 * ({@code @Transactional}). If any step fails (validation, pricing, persistence),
 * the entire operation is rolled back to maintain data consistency.</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * @RestController
 * public class ReservationController {
 *
 *     private final ReservationService reservationService;
 *
 *     @PostMapping("/book")
 *     public ResponseEntity<CreateReservationResponseDTO> bookAccommodation(
 *             @Valid @RequestBody CreateReservationRequestDTO request) {
 *
 *         CreateReservationResponseDTO response =
 *                 reservationService.createReservation(request);
 *
 *         return ResponseEntity.status(HttpStatus.CREATED).body(response);
 *     }
 * }
 * }</pre>
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 * @see ReservationRepository
 * @see AccommodationRepository
 * @see UserService
 * @see ReservationCreatedEvent
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@Service
public class ReservationService {

    private final AccommodationRepository accommodationRepository;
    private final ReservationRepository reservationRepository;
    private final RentalPackageRepository rentalPackageRepository;
    private final UserService userService;
    private final ReservationMapper reservationMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AuthorizationService authorizationService;

    @Value("${stayhub.payment.bank-account}")
    private String bankAccountNumber;

    @Value("${stayhub.payment.deposit-percentage:20}")
    private int depositPercentage;

    @Value("${stayhub.payment.deadline-days:3}")
    private int deadlineDays;

    private static final int MINIMUM_BOOKING_ANTICIPATION_HOURS = 72;
    private static final int MINIMUM_CANCELLATION_ANTICIPATION_HOURS = 48;


    /**
     * Creates a new reservation for a full-accommodation stay.
     *
     * <p>Internal note for the team: one year ago only God and the original
     * implementer knew how this method worked. Now, only God knows.</p>
     *
     * <p>This method orchestrates the reservation creation flow end to end:
     * <ol>
     *   <li>Validates the minimum anticipation rule of 72 hours before check-in</li>
     *   <li>Retrieves and locks the accommodation to serialize competing booking attempts</li>
     *   <li>Checks that no active reservation overlaps the requested stay</li>
     *   <li>Resolves the authenticated user as the guest owner of the reservation</li>
     *   <li>Validates that the stay spans at least one night</li>
     *   <li>Calculates the final reservation total using base nightly pricing and seasonal pricing packages</li>
     *   <li>Calculates the required deposit amount and payment deadline</li>
     *   <li>Persists the reservation and publishes a {@link ReservationCreatedEvent}</li>
     *   <li>Builds the response DTO, including deposit instructions and a seasonal pricing summary</li>
     * </ol>
     *
     * <p><b>Availability check:</b></p>
     * Uses {@link ReservationRepository#existsByAccommodationIdAndDateRange(Long, LocalDateTime, LocalDateTime)}
     * to reject overlaps with active reservations. Two stays overlap when:
     * <pre>
     * existingStart < newEnd AND existingEnd > newStart
     * </pre>
     *
     * <p><b>Seasonal pricing calculation:</b></p>
     * The accommodation {@code pricePerNight} is the default rate. If one or more
     * nights fall inside a {@link RentalPackage}, those nights use the package
     * {@code pricePerNight}. The final total is calculated night by night, which
     * allows mixed stays that are partially inside and partially outside seasonal
     * price ranges.
     *
     * <p>After the final total is computed, the method also calculates the base total
     * without seasonal pricing so it can build a {@code rentalPriceModification}
     * summary for the response. That summary tells the frontend whether the reservation
     * price was reduced, increased, or left unchanged by seasonal pricing.</p>
     *
     * <p><b>Transactional behavior:</b></p>
     * The whole flow runs inside a single transaction. If any validation or
     * persistence step fails, the reservation is not created.
     *
     * <p><b>Main validation outcomes:</b></p>
     * <ul>
     *   <li>{@link AccommodationNotFoundException}: accommodation does not exist, is deleted, or is unavailable</li>
     *   <li>{@link ReservationPolicyViolationException}: check-in is inside the forbidden 72-hour window</li>
     *   <li>{@link IllegalStateException}: the requested stay overlaps an active reservation</li>
     *   <li>{@link IllegalArgumentException}: the stay is shorter than one night</li>
     * </ul>
     *
     * @param createReservationRequestDTO request payload containing accommodation ID,
     *                                    check-in and check-out datetime
     * @return {@link CreateReservationResponseDTO} with reservation data, deposit
     *         instructions, and seasonal pricing modification details
     * @throws AccommodationNotFoundException if the accommodation does not exist,
     *                                        is deleted, or cannot be booked
     * @throws ReservationPolicyViolationException if the reservation is attempted
     *                                             less than 72 hours before check-in
     * @throws IllegalStateException if the requested dates overlap an active reservation
     * @throws IllegalArgumentException if the stay duration is not at least one night
     */
    @Transactional
    public CreateReservationResponseDTO createReservation(
            CreateReservationRequestDTO createReservationRequestDTO) {

        log.info("Processing booking request for accommodation ID: {}",
                createReservationRequestDTO.accommodationId());
        log.debug("Requested dates: {} to {}",
                createReservationRequestDTO.startDate(),
                createReservationRequestDTO.endDate());

        validateMinimumBookingAnticipation(createReservationRequestDTO.startDate());

        // 1. Retrieve and lock the accommodation to serialize competing reservations.
        Accommodation accommodation = accommodationRepository
                .findAvailableByIdWithWriteLock(createReservationRequestDTO.accommodationId())
                .orElseThrow(() -> {
                    log.warn("Booking failed: Accommodation not found or unavailable with ID: {}",
                            createReservationRequestDTO.accommodationId());
                    return new AccommodationNotFoundException("Accommodation not found or unavailable");
                });

        // 2. Check availability (no overlapping active reservations)
        boolean isOverlapping = reservationRepository.existsByAccommodationIdAndDateRange(
                createReservationRequestDTO.accommodationId(),
                createReservationRequestDTO.startDate(),
                createReservationRequestDTO.endDate()
        );

        if (isOverlapping) {
            log.warn("Booking failed: Accommodation {} is already booked for dates {} to {}",
                    accommodation.getId(),
                    createReservationRequestDTO.startDate(),
                    createReservationRequestDTO.endDate());
            throw new IllegalStateException(
                    "Accommodation is already booked for the selected dates");
        }

        // 3. Get a current authenticated user (guest)
        User user = userService.getCurrentUser();
        authorizationService.requireGuestRole(user);
        log.debug("Guest user: {} (ID: {})", user.getEmail(), user.getId());

        // 4. Calculate number of nights and total price
        ReservationPricingDetails pricingDetails = calculateReservationPricing(
                accommodation,
                createReservationRequestDTO.startDate(),
                createReservationRequestDTO.endDate()
        );
        BigDecimal totalPrice = pricingDetails.finalTotalPrice();

        log.debug("Calculated price: {} {} for stay {} to {}",
                totalPrice,
                accommodation.getCurrency(),
                createReservationRequestDTO.startDate(),
                createReservationRequestDTO.endDate());

        // 5. Calculate deposit (20%) and payment deadline
        BigDecimal depositAmount = pricingDetails.depositAmount();
        LocalDateTime paymentDeadline = pricingDetails.paymentDeadline();
        RentalPriceModificationResponseDTO rentalPriceModification =
                pricingDetails.rentalPriceModification();
        log.debug("Deposit amount: {} | Payment deadline: {}", depositAmount, paymentDeadline);

        // 6. Create and populate reservation entity
        Reservation reservation = new Reservation();
        reservation.setStartDate(createReservationRequestDTO.startDate());
        reservation.setEndDate(createReservationRequestDTO.endDate());
        reservation.setAccommodation(accommodation);
        reservation.setGuest(user);
        reservation.setTotalPrice(totalPrice);
        reservation.setCurrency(accommodation.getCurrency());
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.setDepositAmount(depositAmount);
        reservation.setPaymentDeadline(paymentDeadline);
        reservation.setDepositPaid(false);

        // 7. Persist reservation
        Reservation saved = reservationRepository.save(reservation);
        log.info("Reservation created successfully with ID: {}", saved.getId());

        // 8. Publish event for async processing (emails, notifications, etc.)
        applicationEventPublisher.publishEvent(new ReservationCreatedEvent(saved));
        log.debug("ReservationCreatedEvent published for reservation ID: {}", saved.getId());

        // 9. Map to base DTO and enrich with payment details
        CreateReservationResponseDTO base = reservationMapper.toDTO(saved);
        CreateReservationResponseDTO response = new CreateReservationResponseDTO(
                base.id(),
                base.startDate(),
                base.endDate(),
                base.totalPrice(),
                base.currency(),
                base.status(),
                base.accommodationId(),
                base.accommodationTitle(),
                base.userId(),
                depositAmount,
                bankAccountNumber,
                paymentDeadline,
                rentalPriceModification
        );
        log.debug("Booking response prepared for reservation ID: {} | Deposit: {} | Deadline: {}",
                saved.getId(), depositAmount, paymentDeadline);

        return response;
    }

    /*
     * Additional methods that could be added in the future:
     *
     * // Get reservation by ID with authorization check
     * public ReservationResponseDTO getReservation(Long id, String requesterEmail) {
     *     Reservation reservation = reservationRepository.findById(id)
     *             .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
     *
     *     // Only guest or host can view the reservation
     *     if (!reservation.getGuest().getEmail().equals(requesterEmail) &&
     *         !reservation.getAccommodation().getHost().getEmail().equals(requesterEmail)) {
     *         throw new UnauthorizedException("Not authorized to view this reservation");
     *     }
     *
     *     return reservationMapper.toDetailedDTO(reservation);
     * }
     *
     * // Get all reservations for current user (as guest)
     * public List<ReservationSummaryDTO> getMyReservationsAsGuest() {
     *     User currentUser = userService.getCurrentUser();
     *     return reservationRepository.findByGuestId(currentUser.getId())
     *             .stream()
     *             .map(reservationMapper::toSummaryDTO)
     *             .toList();
     * }
     *
     * // Get all reservations for accommodations owned by current user (as host)
     * public List<ReservationSummaryDTO> getMyReservationsAsHost() {
     *     User currentUser = userService.getCurrentUser();
     *     return reservationRepository.findByAccommodationHostId(currentUser.getId())
     *             .stream()
     *             .map(reservationMapper::toSummaryDTO)
     *             .toList();
     * }
     *
     * // Check if user can review an accommodation
     * public boolean canUserReviewAccommodation(Long accommodationId, Long userId) {
     *     // User must have a completed reservation for this accommodation
     *     return reservationRepository.existsByGuestIdAndAccommodationIdAndStatus(
     *             userId, accommodationId, ReservationStatus.COMPLETED);
     * }
     *
     * // Auto-complete past reservations (scheduled job)
     * @Scheduled(cron = "0 0 0 * * ?") // Daily at midnight
     * @Transactional
     * public void autoCompletePastReservations() {
     *     List<Reservation> pastReservations = reservationRepository
     *             .findByStatusAndEndDateBefore(ReservationStatus.ACTIVE, LocalDateTime.now());
     *
     *     pastReservations.forEach(reservation -> {
     *         reservation.setStatus(ReservationStatus.COMPLETED);
     *         reservationRepository.save(reservation);
     *         log.info("Auto-completed reservation ID: {}", reservation.getId());
     *     });
     * }
     *
     */

    @Transactional
    public RetrieveReservationResponseDTO updateReservation(
            Long reservationId,
            UpdateReservationRequestDTO updateReservationRequestDTO) {

        log.info("Updating reservation {} dates to {} - {}",
                reservationId,
                updateReservationRequestDTO.startDate(),
                updateReservationRequestDTO.endDate());

        User currentUser = userService.getCurrentUser();
        authorizationService.requireGuestRole(currentUser);
        Reservation reservation = getReservationForGuestManagement(reservationId, currentUser);

        validateReservationIsActive(reservation);
        validateDepositRequirementForDateChange(reservation, updateReservationRequestDTO.startDate());

        Long accommodationId = reservation.getAccommodation().getId();
        boolean isOverlapping = reservationRepository
                .existsByAccommodationIdAndDateRangeExcludingReservationId(
                        accommodationId,
                        reservationId,
                        updateReservationRequestDTO.startDate(),
                        updateReservationRequestDTO.endDate()
                );

        if (isOverlapping) {
            log.warn("Reservation update failed: accommodation {} is unavailable from {} to {}",
                    accommodationId,
                    updateReservationRequestDTO.startDate(),
                    updateReservationRequestDTO.endDate());
            throw new IllegalStateException(
                    "Accommodation is already booked for the selected dates");
        }

        ReservationPricingDetails pricingDetails = calculateReservationPricing(
                reservation.getAccommodation(),
                updateReservationRequestDTO.startDate(),
                updateReservationRequestDTO.endDate()
        );
        BigDecimal totalPrice = pricingDetails.finalTotalPrice();

        reservation.setStartDate(updateReservationRequestDTO.startDate());
        reservation.setEndDate(updateReservationRequestDTO.endDate());
        reservation.setTotalPrice(totalPrice);
        reservation.setDepositAmount(pricingDetails.depositAmount());

        Reservation updated = reservationRepository.save(reservation);
        log.info("Reservation {} updated successfully", updated.getId());

        return reservationMapper.toRetrieveDTO(updated);
    }

    @Transactional
    public RetrieveReservationResponseDTO cancelReservation(Long reservationId) {

        log.info("Cancelling reservation {}", reservationId);

        User currentUser = userService.getCurrentUser();
        authorizationService.requireGuestRole(currentUser);
        Reservation reservation = getReservationForGuestManagement(reservationId, currentUser);

        validateReservationIsActive(reservation);
        validateCancellationPolicy(reservation);

        reservation.setStatus(ReservationStatus.CANCELLED);

        Reservation cancelled = reservationRepository.save(reservation);
        applicationEventPublisher.publishEvent(new ReservationCancelledEvent(cancelled));

        log.info("Reservation {} cancelled successfully", cancelled.getId());
        return reservationMapper.toRetrieveDTO(cancelled);
    }

    @Transactional
    public RetrieveReservationResponseDTO markDepositAsPaid(Long reservationId) {

        log.info("Marking deposit as paid for reservation {}", reservationId);

        User currentUser = userService.getCurrentUser();
        authorizationService.requireGuestRole(currentUser);
        Reservation reservation = getReservationForGuestManagement(reservationId, currentUser);

        validateReservationIsActive(reservation);

        int updatedRows = reservationRepository.markDepositAsPaidForGuest(
                reservationId,
                currentUser.getId(),
                ReservationStatus.ACTIVE
        );

        if (updatedRows == 0) {
            throw new ReservationPolicyViolationException(
                    "Deposit can only be paid for an active reservation owned by the authenticated guest");
        }

        reservation.setDepositPaid(true);
        log.info("Deposit marked as paid for reservation {}", reservation.getId());

        return reservationMapper.toRetrieveDTO(reservation);
    }

    @Transactional
    public int cancelExpiredUnpaidReservations() {

        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredReservations =
                reservationRepository.findExpiredUnpaidReservations(now);

        expiredReservations.forEach(reservation -> {
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
            applicationEventPublisher.publishEvent(new ReservationCancelledEvent(reservation));
            log.info("Reservation {} cancelled because deposit deadline expired",
                    reservation.getId());
        });

        return expiredReservations.size();
    }

    @Transactional
    public int completePastReservations() {

        LocalDateTime now = LocalDateTime.now();
        List<Reservation> reservationsToComplete =
                reservationRepository.findReservationsToComplete(now);

        reservationsToComplete.forEach(reservation -> {
            reservation.setStatus(ReservationStatus.COMPLETED);
            Reservation completedReservation = reservationRepository.save(reservation);
            applicationEventPublisher.publishEvent(new ReservationCompletedEvent(completedReservation));

            log.info("Reservation {} marked as completed because end date has passed",
                    reservation.getId());

            log.debug("ReservationCompletedEvent published with id: {}", completedReservation.getId());
        });

        return reservationsToComplete.size();
    }

    /**
     * Retrieves the full detail of a single reservation by its ID.
     * Both HOST and GUEST can call this endpoint.
     * The service validates that the authenticated user is either
     * the guest of the reservation OR the host of the accommodation,
     * to prevent unauthorized access to other users' reservations.
     */
    @Transactional(readOnly = true)
    public RetrieveReservationResponseDTO getReservationById(Long reservationId) {

        log.info("Retrieving reservation with ID: {}", reservationId);

        // 1. Get an authenticated user
        User currentUser = userService.getCurrentUser();
        log.debug("Authenticated user: {} (ID: {})", currentUser.getEmail(), currentUser.getId());

        // 2. Find reservation only if the user is authorized (guest or host)
        Reservation reservation = reservationRepository.findAuthorizedById(
                        reservationId,
                        currentUser.getId()
                )
                .orElseThrow(() -> {
                    log.warn("Reservation not found or not accessible with ID: {} for user {}",
                            reservationId, currentUser.getEmail());
                    return new ReservationNotFoundException(
                            "Reservation with ID " + reservationId + " not found"
                    );
                });

        log.info("Reservation {} retrieved successfully by user {}",
                reservationId, currentUser.getEmail());

        // 3. Map to the full detail DTO and return
        return reservationMapper.toRetrieveDTO(reservation);
    }

    @Transactional(readOnly = true)
    public RetrieveReservationResponseDTO getReservationByIdOnAdminActions(Long reservationId) {

        log.info("Retrieving reservation with ID: {} on admin actions", reservationId);

        // 1. Get the authenticated user and require ADMIN role
        User currentUser = userService.getCurrentUser();
        authorizationService.requireAdminRole(currentUser);
        log.debug("Authenticated user: {} (ID: {})", currentUser.getEmail(), currentUser.getId());

        // 2. ADMIN can retrieve any reservation by its ID
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.warn("Reservation not found with ID: {} for admin user {}",
                            reservationId, currentUser.getEmail());
                    return new ReservationNotFoundException(
                            "Reservation with ID " + reservationId + " not found"
                    );
                });

        log.info("Reservation {} retrieved successfully by admin user {}",
                reservationId, currentUser.getEmail());

        // 3. Map to the full detail DTO and return
        return reservationMapper.toRetrieveDTO(reservation);
    }

    /**
     * Retrieves a paginated list of reservations for the authenticated user.
     * If the user has the HOST role, returns reservations for all their accommodations.
     * If the user has the GUEST role, returns their own reservations as a guest.
     * Page size is fixed at 10 results per page.
     */
    @Transactional(readOnly = true)
    public Page<RetrieveReservationSummaryResponseDTO> getMyReservations(int page) {
        return getMyReservations(page, null);
    }

    @Transactional(readOnly = true)
    public Page<RetrieveReservationSummaryResponseDTO> getMyReservations(int page, String scope) {

        log.info("Retrieving reservations page {} for authenticated user with scope '{}'",
                page, scope);

        // 1. Get the authenticated user from the security context
        User currentUser = userService.getCurrentUser();

        log.debug("Authenticated user: {} (ID: {})", currentUser.getEmail(), currentUser.getId());

        // 2. Build pageable with fixed page size of 10, ordered by startDate descending
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "startDate"));

        // 3. Resolve selection strategy
        Page<RetrieveReservationSummaryProjectionDTO> reservations;
        String normalizedScope = scope == null ? "" : scope.trim().toLowerCase();

        if (normalizedScope.isBlank()) {
            // Backward compatibility: host-first behavior
            boolean isHost = authorizationService.canAccessHostReservations(currentUser);
            if (isHost) {
                log.debug("No scope provided; applying legacy host-first behavior for user {}",
                        currentUser.getEmail());
                reservations = reservationRepository
                        .findSummaryByHostId(currentUser.getId(), pageable);
            } else {
                log.debug("No scope provided; applying legacy guest behavior for user {}",
                        currentUser.getEmail());
                reservations = reservationRepository
                        .findSummaryByGuestId(currentUser.getId(), pageable);
            }
        } else {
            reservations = switch (normalizedScope) {
                case "host" -> {
                    log.debug("Scope host selected by user {}", currentUser.getEmail());
                    yield reservationRepository.findSummaryByHostId(currentUser.getId(), pageable);
                }
                case "guest" -> {
                    log.debug("Scope guest selected by user {}", currentUser.getEmail());
                    yield reservationRepository.findSummaryByGuestId(currentUser.getId(), pageable);
                }
                case "all" -> {
                    log.debug("Scope all selected by user {}", currentUser.getEmail());
                    yield reservationRepository
                            .findSummaryByGuestOrHostId(currentUser.getId(), pageable);
                }
                default -> throw new IllegalArgumentException(
                        "Invalid scope value. Allowed values: host, guest, all"
                );
            };
        }

        log.info("Found {} reservations (page {}/{}) for user {}",
                reservations.getNumberOfElements(),
                page,
                reservations.getTotalPages(),
                currentUser.getEmail()
        );

        // 4. Map projection to API DTO preserving the contract
        return reservations.map(reservation -> new RetrieveReservationSummaryResponseDTO(
                reservation.id(),
                reservation.accommodationId(),
                reservation.accommodationTitle(),
                reservation.startDate(),
                reservation.endDate(),
                reservation.totalPrice(),
                reservation.currency().getCurrencyCode(),
                reservation.status()
        ));
    }

    private Reservation getReservationForGuestManagement(Long reservationId, User currentUser) {
        Reservation reservation = reservationRepository.findAuthorizedById(
                        reservationId,
                        currentUser.getId()
                )
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation with ID " + reservationId + " not found"
                ));

        if (!reservation.getGuest().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the guest can manage this reservation");
        }

        return reservation;
    }

    private void validateReservationIsActive(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new ReservationPolicyViolationException(
                    "Only active reservations can be modified or cancelled");
        }
    }

    private void validateMinimumBookingAnticipation(LocalDateTime startDate) {
        LocalDateTime minimumStartDate = LocalDateTime.now()
                .plusHours(MINIMUM_BOOKING_ANTICIPATION_HOURS);

        if (startDate.isBefore(minimumStartDate)) {
            throw new ReservationPolicyViolationException(
                    "Reservations must be created at least 72 hours before check-in");
        }
    }

    private void validateDepositRequirementForDateChange(
            Reservation reservation,
            LocalDateTime newStartDate) {

        LocalDateTime depositRequiredBefore = LocalDateTime.now()
                .plusHours(MINIMUM_BOOKING_ANTICIPATION_HOURS);

        if (newStartDate.isBefore(depositRequiredBefore)
                && !Boolean.TRUE.equals(reservation.getDepositPaid())) {
            throw new DepositNotPaidException(
                    "Deposit must be paid before moving a reservation within 72 hours of check-in");
        }
    }

    private void validateCancellationPolicy(Reservation reservation) {
        LocalDateTime latestCancellationTime = reservation.getStartDate()
                .minusHours(MINIMUM_CANCELLATION_ANTICIPATION_HOURS);

        if (LocalDateTime.now().isAfter(latestCancellationTime)) {
            throw new ReservationPolicyViolationException(
                    "Reservations can only be cancelled at least 48 hours before check-in");
        }
    }

    public ReservationPricingDetails calculateReservationPricing(
            Accommodation accommodation,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        long nights = calculateNights(startDate, endDate, accommodation.getId());
        List<ReservationQuoteBreakdownItem> breakdown = buildPriceBreakdown(accommodation, startDate, endDate);
        BigDecimal finalTotalPrice = breakdown.stream()
                .map(ReservationQuoteBreakdownItem::nightPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal baseTotalPrice = calculateBaseTotalPrice(accommodation, startDate, endDate);
        BigDecimal depositAmount = calculateDepositAmount(finalTotalPrice);
        LocalDateTime paymentDeadline = LocalDateTime.now().plusDays(deadlineDays);
        RentalPriceModificationResponseDTO rentalPriceModification = buildRentalPriceModification(
                baseTotalPrice,
                finalTotalPrice,
                accommodation.getCurrency()
        );

        return new ReservationPricingDetails(
                nights,
                baseTotalPrice,
                finalTotalPrice,
                depositAmount,
                paymentDeadline,
                rentalPriceModification,
                breakdown
        );
    }

    private long calculateNights(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long accommodationId) {

        long nights = ChronoUnit.DAYS.between(
                startDate.toLocalDate(),
                endDate.toLocalDate()
        );

        if (nights <= 0) {
            log.warn("Reservation failed: Invalid stay duration ({} nights) for accommodation {}",
                    nights, accommodationId);
            throw new IllegalArgumentException("Reservation must be at least one night");
        }

        return nights;
    }

    private List<ReservationQuoteBreakdownItem> buildPriceBreakdown(
            Accommodation accommodation,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        LocalDate stayStartDate = startDate.toLocalDate();
        LocalDate stayEndDateInclusive = endDate.toLocalDate().minusDays(1);

        List<RentalPackage> overlappingPackages = rentalPackageRepository.findOverlappingPackagesForStay(
                accommodation.getId(),
                stayStartDate,
                stayEndDateInclusive
        );

        List<ReservationQuoteBreakdownItem> breakdown = new java.util.ArrayList<>();
        LocalDate currentNight = stayStartDate;

        while (currentNight.isBefore(endDate.toLocalDate())) {
            breakdown.add(resolveNightlyPrice(
                    accommodation,
                    overlappingPackages,
                    currentNight
            ));
            currentNight = currentNight.plusDays(1);
        }

        return breakdown;
    }

    private ReservationQuoteBreakdownItem resolveNightlyPrice(
            Accommodation accommodation,
            List<RentalPackage> overlappingPackages,
            LocalDate nightDate) {

        return overlappingPackages.stream()
                .filter(rentalPackage -> !nightDate.isBefore(rentalPackage.getStartDate())
                        && !nightDate.isAfter(rentalPackage.getEndDate()))
                .findFirst()
                .map(rentalPackage -> new ReservationQuoteBreakdownItem(
                        nightDate,
                        rentalPackage.getPricePerNight(),
                        SourceType.SEASONAL
                ))
                .orElseGet(() -> new ReservationQuoteBreakdownItem(
                        nightDate,
                        accommodation.getPricePerNight(),
                        SourceType.BASE
                ));
    }

    private BigDecimal calculateDepositAmount(@NonNull BigDecimal totalPrice) {
        return totalPrice
                .multiply(BigDecimal.valueOf(depositPercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateBaseTotalPrice(
            Accommodation accommodation,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        long nights = ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate());
        return accommodation.getPricePerNight().multiply(BigDecimal.valueOf(nights));
    }

    private RentalPriceModificationResponseDTO buildRentalPriceModification(
            BigDecimal baseTotalPrice,
            BigDecimal finalTotalPrice,
            java.util.Currency currency) {

        int comparison = finalTotalPrice.compareTo(baseTotalPrice);
        BigDecimal difference = finalTotalPrice.subtract(baseTotalPrice).abs();

        if (comparison < 0) {
            return new RentalPriceModificationResponseDTO(
                    RentalPriceModificationType.SAVED,
                    difference,
                    "You saved " + formatAmountForMessage(difference, currency) + " on this reservation."
            );
        }

        if (comparison > 0) {
            return new RentalPriceModificationResponseDTO(
                    RentalPriceModificationType.INCREASED,
                    difference,
                    "The price of this reservation increased " + formatAmountForMessage(difference, currency)
                            + " by seasonal rate."
            );
        }

        return new RentalPriceModificationResponseDTO(
                RentalPriceModificationType.UNCHANGED,
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                "This reservation did not have seasonal price changes."
        );
    }

    private String formatAmountForMessage(BigDecimal amount, java.util.Currency currency) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.of("es", "CO"));

        int fractionDigits = amount.stripTrailingZeros().scale() > 0 ? 2 : 0;

        formatter.setMinimumFractionDigits(fractionDigits);
        formatter.setMaximumFractionDigits(fractionDigits);

        return "$" + formatter.format(amount) + " " + currency.getCurrencyCode();
    }
}

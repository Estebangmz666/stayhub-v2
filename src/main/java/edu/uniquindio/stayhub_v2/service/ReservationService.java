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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

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
 * @author Esteban Gómez León
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
    private final UserService userService;
    private final ReservationMapper reservationMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Creates a new reservation (booking) for accommodation.
     *
     * <p>This method orchestrates the entire reservation creation flow:
     * <ol>
     *   <li>Retrieve and validate the accommodation exists</li>
     *   <li>Check availability for the requested dates (no overlap with existing reservations)</li>
     *   <li>Get the current authenticated user as the guest</li>
     *   <li>Calculate number of nights and total price</li>
     *   <li>Create and persist the reservation entity</li>
     *   <li>Publish a {@link ReservationCreatedEvent} for async processing</li>
     *   <li>Return the reservation details as a DTO</li>
     * </ol>
     *
     * <p><b>Availability Check Algorithm:</b></p>
     * Uses {@link ReservationRepository#existsByAccommodationIdAndDateRange}
     * which checks for overlapping date ranges with existing active reservations.
     * Overlap is detected when:
     * <pre>
     * existingStart < newEnd AND existingEnd > newStart
     * </pre>
     *
     * <p><b>Price Calculation:</b></p>
     * The total price is calculated as:
     * <pre>
     * totalPrice = pricePerNight × numberOfNights
     * </pre>
     * Where {@code numberOfNights} is the difference in days between end date and start date.
     *
     * <p><b>Transactional Behavior:</b></p>
     * <ul>
     *   <li>Everything within this method executes in a single transaction</li>
     *   <li>If any exception is thrown, the transaction rolls back</li>
     *   <li>The event is published after successful commit (via {@code @TransactionalEventListener})</li>
     * </ul>
     *
     * <p><b>Validation Steps:</b></p>
     * <table border="1">
     *   <tr><th>Validation</th><th>Exception</th><th>HTTP Status</th></tr>
     *   <tr><td>Accommodation exists</td><td>AccommodationNotFoundException</td><td>404</td></tr>
     *   <tr><td>Dates don't overlap</td><td>IllegalStateException</td><td>409 Conflict</td></tr>
     *   <tr><td>At least one night</td><td>IllegalArgumentException</td><td>400</td></tr>
     *   <tr><td>User authenticated</td><td>AuthenticationException</td><td>401</td></tr>
     * </table>
     *
     * <p><b>Example Request:</b></p>
     * <pre>{@code
     * {
     *   "accommodationId": 1,
     *   "startDate": "2025-06-01T15:00:00",
     *   "endDate": "2025-06-05T11:00:00"
     * }
     * }</pre>
     *
     * <p><b>Example Response:</b></p>
     * <pre>{@code
     * {
     *   "id": 12345,
     *   "startDate": "2025-06-01T15:00:00",
     *   "endDate": "2025-06-05T11:00:00",
     *   "totalPrice": 1000.00,
     *   "currency": "USD",
     *   "status": "ACTIVE",
     *   "accommodationId": 1,
     *   "accommodationTitle": "Beachfront Villa",
     *   "userId": 678
     * }
     * }</pre>
     *
     * <p><b>Logging:</b></p>
     * Detailed logs are written at each step for monitoring and debugging:
     * <ul>
     *   <li>INFO: Processing booking request</li>
     *   <li>DEBUG: Booking created successfully</li>
     *   <li>WARN: Overlap detected, accommodation not available</li>
     *   <li>ERROR: Unexpected failures during processing</li>
     * </ul>
     *
     * @param createReservationRequestDTO The reservation request containing accommodation ID and dates
     * @return CreateReservationResponseDTO containing the created reservation details
     * @throws AccommodationNotFoundException if the accommodation does not exist or is deleted
     * @throws IllegalStateException if the accommodation is already booked for the requested dates
     * @throws IllegalArgumentException if the reservation is for less than one night
     */
    @Transactional
    public CreateReservationResponseDTO createReservation(
            CreateReservationRequestDTO createReservationRequestDTO) {

        log.info("Processing booking request for accommodation ID: {}",
                createReservationRequestDTO.accommodationId());
        log.debug("Requested dates: {} to {}",
                createReservationRequestDTO.startDate(),
                createReservationRequestDTO.endDate());

        // 1. Retrieve and validate accommodation
        Accommodation accommodation = accommodationRepository
                .findById(createReservationRequestDTO.accommodationId())
                .orElseThrow(() -> {
                    log.warn("Booking failed: Accommodation not found with ID: {}",
                            createReservationRequestDTO.accommodationId());
                    return new AccommodationNotFoundException("Accommodation not found");
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

        // 3. Get current authenticated user (guest)
        User user = userService.getCurrentUser();
        log.debug("Guest user: {} (ID: {})", user.getEmail(), user.getId());

        // 4. Calculate number of nights and total price
        long nights = ChronoUnit.DAYS.between(
                createReservationRequestDTO.startDate().toLocalDate(),
                createReservationRequestDTO.endDate().toLocalDate()
        );

        if (nights <= 0) {
            log.warn("Booking failed: Invalid stay duration ({} nights) for accommodation {}",
                    nights, accommodation.getId());
            throw new IllegalArgumentException("Reservation must be at least one night");
        }

        BigDecimal totalPrice = accommodation.getPricePerNight()
                .multiply(BigDecimal.valueOf(nights));

        log.debug("Calculated price: {} {} for {} nights",
                totalPrice, accommodation.getCurrency(), nights);

        // 5. Create and populate reservation entity
        Reservation reservation = new Reservation();
        reservation.setStartDate(createReservationRequestDTO.startDate());
        reservation.setEndDate(createReservationRequestDTO.endDate());
        reservation.setAccommodation(accommodation);
        reservation.setGuest(user);
        reservation.setTotalPrice(totalPrice);
        reservation.setCurrency(accommodation.getCurrency());
        reservation.setStatus(ReservationStatus.ACTIVE);

        // 6. Persist reservation
        Reservation saved = reservationRepository.save(reservation);
        log.info("Reservation created successfully with ID: {}", saved.getId());

        // 7. Publish event for async processing (emails, notifications, etc.)
        applicationEventPublisher.publishEvent(new ReservationCreatedEvent(saved));
        log.debug("ReservationCreatedEvent published for reservation ID: {}", saved.getId());

        // 8. Map to DTO and return
        CreateReservationResponseDTO response = reservationMapper.toDTO(saved);
        log.debug("Booking response prepared for reservation ID: {}", saved.getId());

        return response;
    }

    /*
     * Additional methods that could be added in the future:
     *
     * // Cancel a reservation
     * @Transactional
     * public void cancelReservation(Long reservationId, String requesterEmail) {
     *     Reservation reservation = reservationRepository.findById(reservationId)
     *             .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
     *
     *     // Validate that requester is either guest or host
     *     if (!reservation.getGuest().getEmail().equals(requesterEmail) &&
     *         !reservation.getAccommodation().getHost().getEmail().equals(requesterEmail)) {
     *         throw new UnauthorizedException("Not authorized to cancel this reservation");
     *     }
     *
     *     // Validate cancellation policy (e.g., cannot cancel within 24h of check-in)
     *     validateCancellationPolicy(reservation);
     *
     *     reservation.setStatus(ReservationStatus.CANCELLED);
     *     reservationRepository.save(reservation);
     *
     *     // Publish cancellation event
     *     applicationEventPublisher.publishEvent(new ReservationCancelledEvent(reservation));
     * }
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
     * // Update reservation dates (if allowed by policy)
     * @Transactional
     * public ReservationResponseDTO updateReservationDates(
     *         Long reservationId,
     *         LocalDateTime newStartDate,
     *         LocalDateTime newEndDate,
     *         String requesterEmail) {
     *
     *     Reservation reservation = reservationRepository.findById(reservationId)
     *             .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
     *
     *     // Only guest can modify (and only if policy allows)
     *     if (!reservation.getGuest().getEmail().equals(requesterEmail)) {
     *         throw new UnauthorizedException("Only guest can modify reservation");
     *     }
     *
     *     // Check if modification is allowed
     *     validateModificationAllowed(reservation);
     *
     *     // Check new dates availability
     *     boolean isOverlapping = reservationRepository.existsByAccommodationIdAndDateRangeExcludingId(
     *             reservation.getAccommodation().getId(),
     *             newStartDate, newEndDate, reservationId);
     *
     *     if (isOverlapping) {
     *         throw new IllegalStateException("New dates are not available");
     *     }
     *
     *     // Recalculate price
     *     long nights = ChronoUnit.DAYS.between(newStartDate.toLocalDate(),
     *                                            newEndDate.toLocalDate());
     *     BigDecimal newTotalPrice = reservation.getAccommodation().getPricePerNight()
     *             .multiply(BigDecimal.valueOf(nights));
     *
     *     reservation.setStartDate(newStartDate);
     *     reservation.setEndDate(newEndDate);
     *     reservation.setTotalPrice(newTotalPrice);
     *
     *     Reservation updated = reservationRepository.save(reservation);
     *
     *     applicationEventPublisher.publishEvent(new ReservationModifiedEvent(updated));
     *
     *     return reservationMapper.toDTO(updated);
     * }
     */
}
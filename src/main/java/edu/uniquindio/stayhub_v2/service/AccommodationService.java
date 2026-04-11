package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.dto.accommodation.AccommodationGetByIdResponseDTO;
import edu.uniquindio.stayhub_v2.exception.AccommodationNotFoundException;
import edu.uniquindio.stayhub_v2.mapper.AccommodationMapper;
import edu.uniquindio.stayhub_v2.model.Accommodation;
import edu.uniquindio.stayhub_v2.repository.AccommodationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import edu.uniquindio.stayhub_v2.exception.ActiveReservationsException;
import edu.uniquindio.stayhub_v2.exception.UnauthorizedHostException;
import edu.uniquindio.stayhub_v2.model.ReservationStatus;
import edu.uniquindio.stayhub_v2.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service class for managing accommodation-related business operations.
 *
 * <p>This service encapsulates all business logic related to accommodations,
 * including retrieval, activation/deactivation, updates, and validation.
 * It acts as an intermediary between the controllers and the data access layer,
 * ensuring that business rules are consistently applied.</p>
 *
 * <p><b>Core Responsibilities:</b></p>
 * <ul>
 *   <li>Retrieving accommodation details by ID</li>
 *   <li>Deactivating (soft-deleting) accommodations with validation</li>
 *   <li>Validating host ownership before modifications</li>
 *   <li>Checking for active reservations before state changes</li>
 *   <li>Coordinating between multiple repositories</li>
 * </ul>
 *
 * <p><b>Business Rules Enforced:</b></p>
 * <ul>
 *   <li>Only the host who owns an accommodation can deactivate it</li>
 *   <li>Accommodations with active future reservations cannot be deactivated</li>
 *   <li>Soft-deleted accommodations are excluded from user-facing queries</li>
 *   <li>Deactivation automatically sets {@code available = false}</li>
 * </ul>
 *
 * <p><b>Transactional Boundaries:</b></p>
 * Write operations are annotated with {@code @Transactional} to ensure
 * data consistency. Read operations are non-transactional for better
 * performance unless specifically required.</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * @RestController
 * public class AccommodationController {
 *
 *     private final AccommodationService accommodationService;
 *
 *     @GetMapping("/accommodations/{id}")
 *     public ResponseEntity<AccommodationGetByIdResponseDTO> getAccommodation(@PathVariable Long id) {
 *         return ResponseEntity.ok(accommodationService.getAccommodation(id));
 *     }
 *
 *     @DeleteMapping("/accommodations/{id}")
 *     public ResponseEntity<Void> deactivateAccommodation(
 *             @PathVariable Long id,
 *             @AuthenticationPrincipal UserDetails userDetails) {
 *         accommodationService.deactivateAccommodation(id, userDetails.getUsername());
 *         return ResponseEntity.noContent().build();
 *     }
 * }
 * }</pre>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see AccommodationRepository
 * @see ReservationRepository
 * @see AccommodationMapper
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;
    private final ReservationRepository reservationRepository;

    /**
     * Retrieves an active accommodation by its unique identifier.
     *
     * <p>This method fetches accommodation details for display purposes.
     * It only returns accommodations that have not been soft-deleted.
     * The result is mapped to a DTO to avoid exposing internal entity
     * structure to the presentation layer.</p>
     *
     * <p><b>Query Details:</b></p>
     * Uses {@link AccommodationRepository#findByIdAndDeletedFalse(Long)}
     * which automatically filters out soft-deleted accommodations.
     *
     * <p><b>Performance:</b> This is a read-only operation that uses the
     * primary key index for fast lookup. No transaction is required.</p>
     *
     * <p><b>Example Response:</b></p>
     * <pre>{@code
     * {
     *   "id": 1,
     *   "title": "Beachfront Villa",
     *   "description": "Beautiful villa with ocean view",
     *   "capacity": 6,
     *   "pricePerNight": 250.00,
     *   "currency": "USD",
     *   "city": "Cartagena",
     *   "mainImage": "https://...",
     *   "images": ["https://...", "https://..."],
     *   "host": {
     *     "id": 123,
     *     "fullName": "John Doe"
     *   }
     * }
     * }</pre>
     *
     * @param id The unique identifier of the accommodation to retrieve
     * @return AccommodationGetByIdResponseDTO containing the accommodation details
     * @throws AccommodationNotFoundException if no active accommodation exists with the given ID
     */
    public AccommodationGetByIdResponseDTO getAccommodation(Long id) {
        log.debug("Retrieving accommodation with ID: {}", id);

        Accommodation accommodation = accommodationRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("Accommodation not found or deleted with ID: {}", id);
                    return new AccommodationNotFoundException("Accommodation not found with id: " + id);
                });

        log.debug("Successfully retrieved accommodation: {} (ID: {})",
                accommodation.getTitle(), id);

        return accommodationMapper.toAccommodationGetByIdResponseDTO(accommodation);
    }

    /**
     * Deactivates (soft-deletes) accommodation after validating ownership
     * and checking for active future reservations.
     *
     * <p>This method performs a soft delete by setting the {@code deleted} flag
     * to {@code true} and {@code available} to {@code false}. The accommodation
     * remains in the database for historical and referential integrity purposes
     * but is hidden from all user-facing queries.</p>
     *
     * <p><b>Business Rules Validated:</b></p>
     * <ol>
     *   <li><b>Ownership:</b> Only the host who owns the accommodation can deactivate it</li>
     *   <li><b>No Active Reservations:</b> Cannot deactivate if there are active
     *       reservations with start dates in the future</li>
     * </ol>
     *
     * <p><b>Transactional:</b> This method is annotated with {@code @Transactional}
     * to ensure that the deactivation is atomic. If any validation fails, the
     * transaction is rolled back.</p>
     *
     * <p><b>State Changes:</b></p>
     * <ul>
     *   <li>{@code deleted}: {@code false} → {@code true}</li>
     *   <li>{@code available}: {@code true/false} → {@code false}</li>
     *   <li>{@code updatedAt}: Automatically set to current timestamp</li>
     * </ul>
     *
     * <p><b>Effects of Deactivation:</b></p>
     * <ul>
     *   <li>Accommodation no longer appears in search results</li>
     *   <li>New reservations cannot be created</li>
     *   <li>Existing past reservations remain visible in user history</li>
     *   <li>Host cannot reactivate without admin intervention (by design)</li>
     * </ul>
     *
     * <p><b>Usage Example:</b></p>
     * <pre>{@code
     * // In a controller method
     * @DeleteMapping("/{id}")
     * public ResponseEntity<Void> deactivate(
     *         @PathVariable Long id,
     *         @AuthenticationPrincipal UserDetails user) {
     *
     *     accommodationService.deactivateAccommodation(id, user.getUsername());
     *     return ResponseEntity.noContent().build();
     * }
     * }</pre>
     *
     * <p><b>Security Considerations:</b></p>
     * <ul>
     *   <li>The requester email should come from the authenticated user context</li>
     *   <li>Never accept the email from the request body/parameters directly</li>
     *   <li>Use {@code @AuthenticationPrincipal} or SecurityContextHolder</li>
     * </ul>
     *
     * <p><b>Error Scenarios:</b></p>
     * <table border="1">
     *   <tr><th>Scenario</th><th>Exception Thrown</th><th>HTTP Status</th></tr>
     *   <tr><td>Accommodation not found</td><td>AccommodationNotFoundException</td><td>404</td></tr>
     *   <tr><td>User is not the host</td><td>UnauthorizedHostException</td><td>403</td></tr>
     *   <tr><td>Has future active reservations</td><td>ActiveReservationsException</td><td>400</td></tr>
     * </table>
     *
     * @param id The unique identifier of the accommodation to deactivate
     * @param requesterEmail The email of the authenticated user making the request
     * @throws AccommodationNotFoundException if no active accommodation exists with the given ID
     * @throws UnauthorizedHostException if the requester is not the host of the accommodation
     * @throws ActiveReservationsException if the accommodation has active future reservations
     */
    @Transactional
    public void deactivateAccommodation(Long id, String requesterEmail) {
        log.info("Processing deactivation request for accommodation ID: {} by user: {}",
                id, requesterEmail);

        // 1. Retrieve and validate accommodation existence
        Accommodation accommodation = accommodationRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("Deactivation failed: Accommodation not found with ID: {}", id);
                    return new AccommodationNotFoundException("Accommodation not found with id: " + id);
                });

        // 2. Validate host ownership
        if (!accommodation.getHost().getEmail().equals(requesterEmail)) {
            log.warn("Deactivation denied: User {} attempted to deactivate accommodation {} owned by {}",
                    requesterEmail, id, accommodation.getHost().getEmail());
            throw new UnauthorizedHostException(
                    "No tienes permisos para dar de baja esta casa rural.");
        }

        // 3. Check for future active reservations
        boolean hasFutureReservations = reservationRepository
                .existsByAccommodationIdAndStartDateAfterAndStatus(
                        id, LocalDateTime.now(), ReservationStatus.ACTIVE);

        if (hasFutureReservations) {
            log.warn("Deactivation blocked: Accommodation {} has active future reservations", id);
            throw new ActiveReservationsException(
                    "No se puede dar de baja una casa con reservas futuras");
        }

        // 4. Perform soft deletion
        accommodation.setDeleted(true);
        accommodation.setAvailable(false);
        accommodationRepository.save(accommodation);

        log.info("Accommodation {} successfully deactivated by user {}", id, requesterEmail);
    }

    /*
     * Additional methods that could be added in the future:
     *
     * // Create a new accommodation
     * @Transactional
     * public AccommodationResponseDTO createAccommodation(
     *         AccommodationCreateRequestDTO request,
     *         String hostEmail) {
     *     // Implementation
     * }
     *
     * // Update accommodation details
     * @Transactional
     * public AccommodationResponseDTO updateAccommodation(
     *         Long id,
     *         AccommodationUpdateRequestDTO request,
     *         String hostEmail) {
     *     // Implementation with ownership validation
     * }
     *
     * // Reactivate a deactivated accommodation (admin only)
     * @Transactional
     * public void reactivateAccommodation(Long id) {
     *     // Admin-only operation
     * }
     *
     * // Search accommodations with filters
     * public Page<AccommodationSearchResponseDTO> searchAccommodations(
     *         AccommodationSearchCriteria criteria,
     *         Pageable pageable) {
     *     // Implementation with pagination
     * }
     *
     * // Check availability for date range
     * public boolean isAccommodationAvailable(
     *         Long accommodationId,
     *         LocalDateTime startDate,
     *         LocalDateTime endDate) {
     *     return !reservationRepository.existsByAccommodationIdAndDateRange(
     *             accommodationId, startDate, endDate);
     * }
     *
     * // Get accommodations by host
     * public List<AccommodationSummaryDTO> getAccommodationsByHost(String hostEmail) {
     *     // Implementation
     * }
     *
     * // Update availability status
     * @Transactional
     * public void updateAvailability(Long id, boolean available, String hostEmail) {
     *     // Implementation with ownership validation
     * }
     *
     * // Update pricing
     * @Transactional
     * public void updatePricing(Long id, BigDecimal newPrice, String hostEmail) {
     *     // Validate no active reservations before allowing price change
     *     // Or apply price change only to future reservations
     * }
     */
}
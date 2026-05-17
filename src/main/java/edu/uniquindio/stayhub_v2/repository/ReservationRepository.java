package edu.uniquindio.stayhub_v2.repository;

import edu.uniquindio.stayhub_v2.dto.accommodation.UnavailableAccommodationDateRangeResponseDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.RetrieveReservationSummaryProjectionDTO;
import edu.uniquindio.stayhub_v2.model.Reservation;
import edu.uniquindio.stayhub_v2.model.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Reservation} entities.
 *
 * <p>This interface provides CRUD operations and custom query methods for
 * accessing and manipulating reservation data in the database. It extends
 * Spring Data JPA's {@link JpaRepository}, which provides standard database
 * operations out of the box.</p>
 *
 * <p><b>Inherited Methods (from JpaRepository):</b></p>
 * <ul>
 *   <li>{@code save(Reservation)} - Persist or update a reservation</li>
 *   <li>{@code findById(Long)} - Find reservation by ID</li>
 *   <li>{@code findAll()} - Retrieve all reservations</li>
 *   <li>{@code delete(Reservation)} - Delete a reservation</li>
 *   <li>{@code existsById(Long)} - Check if reservation exists by ID</li>
 * </ul>
 *
 * <p><b>Core Business Queries:</b></p>
 * This repository includes specialized queries for availability checking,
 * which is critical for preventing double bookings and ensuring data integrity.
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * @Service
 * public class ReservationService {
 *
 *     private final ReservationRepository reservationRepository;
 *
 *     public boolean isAccommodationAvailable(Long accommodationId,
 *                                             LocalDateTime start,
 *                                             LocalDateTime end) {
 *         return !reservationRepository.existsByAccommodationIdAndDateRange(
 *             accommodationId, start, end);
 *     }
 *
 *     public boolean hasActiveReservationsAfter(Long accommodationId, LocalDate date) {
 *         return reservationRepository.existsByAccommodationIdAndStartDateAfterAndStatus(
 *             accommodationId, date, ReservationStatus.ACTIVE);
 *     }
 * }
 * }</pre>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see JpaRepository
 * @see Reservation
 * @see ReservationStatus
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Checks if an accommodation has active reservations starting after a given date and time.
     *
     * <p>This method is used to determine if an accommodation can be safely modified
     * or deleted. If there are active reservations starting after the specified datetime,
     * certain operations (like deleting the accommodation) may be blocked to protect
     * existing bookings.</p>
     *
     * <p><b>Query Logic:</b></p>
     * <pre>
     * SELECT COUNT(r) > 0 FROM Reservation r
     * WHERE r.accommodation.id = :accommodationId
     *   AND r.startDate > :startDate
     *   AND r.status = :status
     * </pre>
     *
     * <p><b>Use Cases:</b></p>
     * <ul>
     *   <li>Validating if an accommodation can be deleted (check for any future reservations)</li>
     *   <li>Checking if price changes affect existing bookings</li>
     *   <li>Determining if an accommodation can be marked as unavailable</li>
     *   <li>Validating if a host can modify check-in/out hours for existing reservations</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * // Check if there are active reservations starting after now
     * boolean hasFutureBookings = repository
     *     .existsByAccommodationIdAndStartDateAfterAndStatus(
     *         accommodationId, LocalDateTime.now(), ReservationStatus.ACTIVE);
     *
     * if (hasFutureBookings) {
     *     throw new ActiveReservationsException(
     *         "Cannot delete accommodation with future reservations");
     * }
     *
     * // Check if there are reservations starting after a specific date
     * LocalDateTime cutoffDate = LocalDateTime.of(2025, 12, 31, 23, 59);
     * boolean hasNextYearBookings = repository
     *     .existsByAccommodationIdAndStartDateAfterAndStatus(
     *         accommodationId, cutoffDate, ReservationStatus.ACTIVE);
     * }</pre>
     *
     * <p><b>Precision Note:</b> Since this method uses {@link LocalDateTime}, it provides
     * hour-level precision. A reservation starting at 15:00 on a given day will be
     * considered "after" a cutoff of 11:00 on the same day, allowing for same-day
     * operations like check-out cleaning windows.</p>
     *
     * @param accommodationId The ID of the accommodation to check
     * @param startDate The cutoff datetime - checks for reservations starting strictly AFTER this datetime
     * @param status The reservation status to filter by (typically {@link ReservationStatus#ACTIVE})
     * @return {@code true} if at least one matching reservation exists, {@code false} otherwise
     */
    boolean existsByAccommodationIdAndStartDateAfterAndStatus(
            Long accommodationId,
            LocalDateTime startDate,
            ReservationStatus status);

    /**
     * Checks if accommodation has any active reservation that overlaps with
     * a specified date range.
     *
     * <p>This is the primary method for validating accommodation availability
     * before creating a new reservation. It prevents double bookings by checking
     * for overlapping date ranges with existing active reservations.</p>
     *
     * <p><b>Query Logic (JPQL):</b></p>
     * <pre>
     * SELECT COUNT(r) > 0 FROM Reservation r
     * WHERE r.accommodation.id = :accommodationId
     *   AND r.status = 'ACTIVE'
     *   AND (r.startDate < :endDate AND r.endDate > :startDate)
     * </pre>
     *
     * <p><b>Overlap Detection Algorithm:</b></p>
     * Two date ranges overlap if:
     * <pre>
     * existingStart < newEnd AND existingEnd > newStart
     * </pre>
     *
     * <p><b>Visual Example:</b></p>
     * <pre>
     * Existing:   |-------|
     * New:     |-------|        → Overlap ✓
     * New:            |-------| → No Overlap ✗
     * New:     |---------------| → Overlap ✓
     * </pre>
     *
     * <p><b>Use Cases:</b></p>
     * <ul>
     *   <li>Validating availability before creating a reservation</li>
     *   <li>Checking if date modifications are possible</li>
     *   <li>Displaying blocked dates in a calendar UI</li>
     * </ul>
     *
     * <p><b>Performance Considerations:</b></p>
     * <ul>
     *   <li>This query uses JPQL for database-agnostic operation</li>
     *   <li>Consider adding database indexes on (accommodation_id, start_date, end_date, status)</li>
     *   <li>For high-traffic scenarios, consider caching availability</li>
     * </ul>
     *
     * <p><b>Example Usage:</b></p>
     * <pre>{@code
     * public void validateAvailability(Long accommodationId,
     *                                  LocalDateTime checkIn,
     *                                  LocalDateTime checkOut) {
     *
     *     boolean isUnavailable = reservationRepository
     *         .existsByAccommodationIdAndDateRange(accommodationId, checkIn, checkOut);
     *
     *     if (isUnavailable) {
     *         throw new IllegalStateException(
     *             "Accommodation is not available for the selected dates");
     *     }
     * }
     * }</pre>
     *
     * <p><b>Edge Cases Handled:</b></p>
     * <ul>
     *   <li>Exact same dates (back-to-back): No overlap (end date of one = start date of another)</li>
     *   <li>Reservation starting exactly when another ends: No overlap</li>
     *   <li>Partial day overlaps: Detected correctly</li>
     * </ul>
     *
     * @param accommodationId The ID of the accommodation to check availability for
     * @param startDate The proposed check-in date and time
     * @param endDate The proposed check-out date and time
     * @return {@code true} if there is an overlapping active reservation,
     *         {@code false} if the accommodation is available
     */
    @Query("""
    SELECT COUNT(r) > 0 FROM Reservation r
    WHERE r.accommodation.id = :accommodationId
    AND r.status = 'ACTIVE'
    AND (
        (r.startDate < :endDate AND r.endDate > :startDate)
    )
""")
    boolean existsByAccommodationIdAndDateRange(
            @Param("accommodationId") Long accommodationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
    SELECT COUNT(r) > 0 FROM Reservation r
    WHERE r.accommodation.id = :accommodationId
    AND r.id <> :reservationId
    AND r.status = 'ACTIVE'
    AND (
        (r.startDate < :endDate AND r.endDate > :startDate)
    )
""")
    boolean existsByAccommodationIdAndDateRangeExcludingReservationId(
            @Param("accommodationId") Long accommodationId,
            @Param("reservationId") Long reservationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Finds all active reservations of a guest that still have a pending deposit payment.
     *
     * <p>Used for in-app notifications: when the guest logs in, the frontend queries
     * this endpoint to display pending payment alerts.</p>
     *
     * @param guestId The ID of the authenticated guest
     * @param now     Current datetime to filter only non-expired deadlines
     * @return List of reservations with pending deposit payment
     */
    @Query("""
    SELECT r FROM Reservation r
    WHERE r.guest.id = :guestId
    AND r.depositPaid = false
    AND r.status = 'ACTIVE'
    AND r.paymentDeadline >= :now
    ORDER BY r.paymentDeadline ASC
""")
    List<Reservation> findPendingDepositsByGuest(
            @Param("guestId") Long guestId,
            @Param("now") LocalDateTime now
    );

    /**
     * Finds active reservations with a deposit deadline falling within a specific time window.
     *
     * <p>Used by the payment reminder scheduler to identify reservations whose
     * payment deadline is approaching (e.g., within the next 24 hours) so that
     * reminder emails can be sent to the guests.</p>
     *
     * @param from Start of the time window
     * @param to   End of the time window
     * @return List of reservations with a payment deadline in the window
     */
    @Query("""
    SELECT r FROM Reservation r
    WHERE r.depositPaid = false
    AND r.status = 'ACTIVE'
    AND r.paymentDeadline >= :from
    AND r.paymentDeadline <= :to
""")
    List<Reservation> findReservationsWithPaymentDeadlineApproaching(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
    SELECT r FROM Reservation r
    WHERE r.depositPaid = false
    AND r.status = 'ACTIVE'
    AND r.paymentDeadline < :now
""")
    List<Reservation> findExpiredUnpaidReservations(@Param("now") LocalDateTime now);

    @Query("""
    SELECT r FROM Reservation r
    WHERE r.status = 'ACTIVE'
    AND r.endDate < :now
""")
    List<Reservation> findReservationsToComplete(@Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE Reservation r
    SET r.depositPaid = true
    WHERE r.id = :reservationId
    AND r.guest.id = :guestId
    AND r.status = :activeStatus
""")
    int markDepositAsPaidForGuest(
            @Param("reservationId") Long reservationId,
            @Param("guestId") Long guestId,
            @Param("activeStatus") ReservationStatus activeStatus
    );

    @Query("""
    SELECT r FROM Reservation r
    WHERE r.id = :reservationId
    AND (
        r.guest.id = :userId
        OR r.accommodation.host.id = :userId
    )
""")
    Optional<Reservation> findAuthorizedById(
            @Param("reservationId") Long reservationId,
            @Param("userId") Long userId
    );

    @Query("""
    SELECT new edu.uniquindio.stayhub_v2.dto.reservation.RetrieveReservationSummaryProjectionDTO(
        r.id,
        r.accommodation.id,
        r.accommodation.title,
        r.startDate,
        r.endDate,
        r.totalPrice,
        r.currency,
        r.status
    )
    FROM Reservation r
    WHERE r.guest.id = :guestId
""")
    Page<RetrieveReservationSummaryProjectionDTO> findSummaryByGuestId(
            @Param("guestId") Long guestId,
            Pageable pageable
    );

    @Query("""
    SELECT new edu.uniquindio.stayhub_v2.dto.reservation.RetrieveReservationSummaryProjectionDTO(
        r.id,
        r.accommodation.id,
        r.accommodation.title,
        r.startDate,
        r.endDate,
        r.totalPrice,
        r.currency,
        r.status
    )
    FROM Reservation r
    WHERE r.accommodation.host.id = :hostId
""")
    Page<RetrieveReservationSummaryProjectionDTO> findSummaryByHostId(
            @Param("hostId") Long hostId,
            Pageable pageable
    );

    @Query(
            value = """
        SELECT DISTINCT new edu.uniquindio.stayhub_v2.dto.reservation.RetrieveReservationSummaryProjectionDTO(
            r.id,
            r.accommodation.id,
            r.accommodation.title,
            r.startDate,
            r.endDate,
            r.totalPrice,
            r.currency,
            r.status
        )
        FROM Reservation r
        WHERE r.guest.id = :userId
        OR r.accommodation.host.id = :userId
    """,
            countQuery = """
        SELECT COUNT(DISTINCT r.id) FROM Reservation r
        WHERE r.guest.id = :userId
        OR r.accommodation.host.id = :userId
    """
    )
    Page<RetrieveReservationSummaryProjectionDTO> findSummaryByGuestOrHostId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
    SELECT new edu.uniquindio.stayhub_v2.dto.accommodation.UnavailableAccommodationDateRangeResponseDTO(
        r.startDate,
        r.endDate
    )
    FROM Reservation r
    WHERE r.accommodation.id = :accommodationId
    AND r.status = 'ACTIVE'
    ORDER BY r.startDate ASC
""")
    Page<UnavailableAccommodationDateRangeResponseDTO> findUnavailableDateRangesByAccommodationId(
            @Param("accommodationId") Long accommodationId,
            Pageable pageable
    );

    /*
     * Additional query methods that could be added in the future:
     *
     * // Find all reservations for a specific guest
     * List<Reservation> findByGuestId(Long guestId);
     *
     * // Find all reservations for a specific accommodation
     * List<Reservation> findByAccommodationId(Long accommodationId);
     *
     * // Find active reservations for a guest
     * List<Reservation> findByGuestIdAndStatus(Long guestId, ReservationStatus status);
     *
     * // Count reservations by status for an accommodation
     * long countByAccommodationIdAndStatus(Long accommodationId, ReservationStatus status);
     *
     * // Find overlapping reservations (including canceled/completed for auditing)
     * @Query("""
     *     SELECT r FROM Reservation r
     *     WHERE r.accommodation.id = :accommodationId
     *     AND (
     *         (r.startDate < :endDate AND r.endDate > :startDate)
     *     )
     * """)
     * List<Reservation> findOverlappingReservations(
     *     @Param("accommodationId") Long accommodationId,
     *     @Param("startDate") LocalDateTime startDate,
     *     @Param("endDate") LocalDateTime endDate);
     */
}

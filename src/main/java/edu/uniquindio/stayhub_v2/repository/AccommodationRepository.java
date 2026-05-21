package edu.uniquindio.stayhub_v2.repository;

import edu.uniquindio.stayhub_v2.dto.host.HostAccommodationMetricsProjection;
import edu.uniquindio.stayhub_v2.model.Accommodation;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.time.LocalDateTime;

/**
 * Repository interface for managing {@link Accommodation} entities.
 *
 * <p>This interface provides CRUD operations and custom query methods for
 * accessing and manipulating accommodation data in the database. It extends
 * Spring Data JPA's {@link JpaRepository}, which provides standard database
 * operations out of the box.</p>
 *
 * <p><b>Inherited Methods (from JpaRepository):</b></p>
 * <ul>
 *   <li>{@code save(Accommodation)} - Persist or update an accommodation</li>
 *   <li>{@code findById(Long)} - Find accommodation by ID (including soft-deleted)</li>
 *   <li>{@code findAll()} - Retrieve all accommodations (including soft-deleted)</li>
 *   <li>{@code delete(Accommodation)} - Hard delete (use with caution)</li>
 *   <li>{@code existsById(Long)} - Check if accommodation exists by ID</li>
 *   <li>{@code count()} - Count total accommodations</li>
 * </ul>
 *
 * <p><b>Soft-Delete Consideration:</b></p>
 * Most queries should exclude soft-deleted accommodations using the
 * {@code deleted = false} condition. The custom method
 * {@link #findByIdAndDeletedFalse(Long)} provides this functionality.
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * @Service
 * public class AccommodationService {
 *
 *     private final AccommodationRepository accommodationRepository;
 *
 *     public Accommodation getActiveAccommodation(Long id) {
 *         return accommodationRepository.findByIdAndDeletedFalse(id)
 *                 .orElseThrow(() -> new AccommodationNotFoundException(
 *                     "Accommodation not found or deleted: " + id));
 *     }
 *
 *     public Accommodation saveAccommodation(Accommodation accommodation) {
 *         return accommodationRepository.save(accommodation);
 *     }
 * }
 * }</pre>
 *
 * <p><b>Query Derivation:</b></p>
 * Spring Data JPA automatically implements query methods based on method names.
 * For example, {@code findByIdAndDeletedFalse} translates to:
 * <pre>
 * SELECT a FROM Accommodation a WHERE a.id = :id AND a.deleted = false
 * </pre>
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 * @see JpaRepository
 * @see Accommodation
 */
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    /**
     * Finds an active (non-deleted) accommodation by its unique identifier.
     *
     * <p>This method queries the database for accommodation with the specified
     * ID that has NOT been soft-deleted. It is the preferred method for retrieving
     * accommodations for display to users, as soft-deleted accommodations should
     * not be visible in the application.</p>
     *
     * <p><b>Query Logic:</b></p>
     * <pre>
     * SELECT * FROM accommodations
     * WHERE id = :id AND deleted = false
     * </pre>
     *
     * <p><b>Return Value:</b></p>
     * <ul>
     *   <li>{@code Optional<Accommodation>} - Empty if accommodation doesn't exist or is deleted</li>
     *   <li>Populated {@code Optional} if active accommodation is found</li>
     * </ul>
     *
     * <p><b>Usage Pattern:</b></p>
     * <pre>{@code
     * accommodationRepository.findByIdAndDeletedFalse(id)
     *     .ifPresent(accommodation -> {
     *         // Process active accommodation
     *     });
     *
     * // Or with exception handling
     * Accommodation accommodation = accommodationRepository
     *     .findByIdAndDeletedFalse(id)
     *     .orElseThrow(() -> new AccommodationNotFoundException(
     *         "Active accommodation not found with id: " + id));
     * }</pre>
     *
     * <p><b>Performance:</b> Uses the primary key index plus the deleted flag,
     * making this query very efficient.</p>
     *
     * @param id The unique identifier of the accommodation to find (must not be null)
     * @return An {@code Optional} containing the found accommodation if it exists
     *         and is not deleted, otherwise an empty {@code Optional}
     */
    Optional<Accommodation> findByIdAndDeletedFalse(Long id);

    /**
     * Finds an available accommodation by ID with a pessimistic write lock.
     *
     * <p>This method acquires a {@link LockModeType#PESSIMISTIC_WRITE} lock on the
     * database row to prevent concurrent modifications. It is essential for
     * booking operations where race conditions could lead to double-booking.</p>
     *
     * <p><b>Lock Behavior:</b></p>
     * <ul>
     *   <li>The lock is held until the transaction completes</li>
     *   <li>Other transactions attempting to lock the same row will block</li>
     *   <li>Prevents concurrent updates to the availability status</li>
     * </ul>
     *
     * <p><b>Query Conditions:</b></p>
     * <ul>
     *   <li>Accommodation must NOT be soft-deleted ({@code deleted = false})</li>
     *   <li>Accommodation must be available ({@code available = true})</li>
     *   <li>Accommodation must exist with the specified ID</li>
     * </ul>
     *
     * <p><b>Usage Pattern:</b></p>
     * <pre>{@code
     * @Transactional
     * public Reservation bookAccommodation(Long accommodationId) {
     *     Accommodation accommodation = accommodationRepository
     *         .findAvailableByIdWithWriteLock(accommodationId)
     *         .orElseThrow(() -> new AccommodationNotAvailableException(
     *             "Accommodation not found or not available: " + accommodationId));
     *
     *     // Check availability, create reservation, update accommodation status
     *     accommodation.setAvailable(false);
     *     return accommodationRepository.save(accommodation);
     * }
     * }</pre>
     *
     * <p><b>Important:</b> This method MUST be called within a transactional context
     * ({@code @Transactional}) for the lock to be properly managed.</p>
     *
     * @param id The unique identifier of the accommodation to find and lock
     * @return An {@code Optional} containing the locked accommodation if found,
     *         otherwise an empty {@code Optional}
     * @throws org.springframework.dao.CannotAcquireLockException if the lock cannot be acquired
     * @see LockModeType#PESSIMISTIC_WRITE
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT a FROM Accommodation a
    WHERE a.id = :id
    AND a.deleted = false
    AND a.available = true
""")
    Optional<Accommodation> findAvailableByIdWithWriteLock(@Param("id") Long id);

    /**
     * Retrieves a paginated list of available accommodations in a specific city.
     *
     * <p>This method performs a case-insensitive search for accommodations matching
     * the city name, filtering out soft-deleted accommodations and those marked
     * as unavailable.</p>
     *
     * <p><b>Filtering Logic:</b></p>
     * <ul>
     *   <li>City name matching (case-insensitive, partial matches are possible)</li>
     *   <li>Only non-deleted accommodations ({@code deleted = false})</li>
     *   <li>Only available accommodations ({@code available = true})</li>
     * </ul>
     *
     * <p><b>Pagination:</b> Results are returned in a {@link Page} object that
     * includes metadata about the total number of records, current page, and
     * sorting information.</p>
     *
     * <p><b>Usage Pattern:</b></p>
     * <pre>{@code
     * Pageable pageable = PageRequest.of(0, 20, Sort.by("title").ascending());
     * Page<Accommodation> results = accommodationRepository
     *     .findByCityContainingIgnoreCaseAndDeletedFalseAndAvailableTrue(
     *         "New York", pageable);
     *
     * System.out.println("Total available: " + results.getTotalElements());
     * results.forEach(accommodation -> {
     *     // Process each accommodation
     * });
     * }</pre>
     *
     * @param city The city name to search for (case-insensitive, can be partial)
     * @param pageable Pagination information (page number, page size, sorting)
     * @return A {@code Page} of available accommodations matching the city criteria,
     *         never null (empty page if no matches found)
     * @throws IllegalArgumentException if pageable is null
     */
    Page<Accommodation> findByCityContainingIgnoreCaseAndDeletedFalseAndAvailableTrue(
            String city,
            Pageable pageable);

    /**
     * Retrieves aggregated metrics for a host's accommodations within a specific
     * date period.
     *
     * <p>This complex native query provides comprehensive statistics about
     * reservations and reviews for all active accommodations belonging to a host.
     * It joins reservation and review data to calculate key performance metrics.</p>
     *
     * <p><b>Metrics Calculated:</b></p>
     * <ul>
     *   <li><b>Reservation Metrics:</b>
     *     <ul>
     *       <li>Total reservations in period</li>
     *       <li>Active reservations in period</li>
     *       <li>Cancelled reservations in period</li>
     *       <li>Reserved revenue in period</li>
     *       <li>Count of paid deposits</li>
     *       <li>Total amount of paid deposits</li>
     *     </ul>
     *   </li>
     *   <li><b>Review Metrics:</b>
     *     <ul>
     *       <li>Average rating across all reviews</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p><b>Period Logic:</b></p>
     * <ul>
     *   <li>Reservations are included if they overlap with the period:
     *       {@code start_date < periodEndExclusive AND end_date >= periodStart}</li>
     *   <li>The period end is exclusive ({@code <}) to define clean boundaries</li>
     *   <li>The period start is inclusive ({@code >=})</li>
     * </ul>
     *
     * <p><b>Query Behavior:</b></p>
     * <ul>
     *   <li>Returns all non-deleted accommodations for the host, even those with
     *       no reservations (metrics will be zero)</li>
     *   <li>Accommodations with no reviews will have null average rating (may be
     *       zero or null in projection)</li>
     *   <li>Results are ordered alphabetically by accommodation title</li>
     *   <li>Results are paginated for performance</li>
     * </ul>
     *
     * <p><b>Usage Pattern:</b></p>
     * <pre>{@code
     * LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
     * LocalDateTime endDate = LocalDateTime.now();
     * Pageable pageable = PageRequest.of(0, 10);
     *
     * Page<HostAccommodationMetricsProjection> metrics = accommodationRepository
     *     .findHostAccommodationMetricsByPeriod(hostId, startDate, endDate, pageable);
     *
     * metrics.forEach(metric -> {
     *     System.out.println("Accommodation: " + metric.getAccommodationTitle());
     *     System.out.println("Total Revenue: " + metric.getReservedRevenueInPeriod());
     *     System.out.println("Average Rating: " + metric.getAverageRating());
     * });
     * }</pre>
     *
     * @param hostId The ID of the host whose accommodations to analyze (must not be null)
     * @param periodStart The start date-time of the analysis period (inclusive)
     * @param periodEndExclusive The end date-time of the analysis period (exclusive)
     * @param pageable Pagination information for the results
     * @return A paginated list of metric projections for the host's accommodations,
     *         never null
     * @throws IllegalArgumentException if any parameter is null
     */
    @Query(
            value = """
            SELECT
                a.id AS "accommodationId",
                a.title AS "accommodationTitle",
                a.city AS "city",
                a.currency AS "currency",
                COALESCE(reservation_metrics.total_reservations_in_period, 0) AS "totalReservationsInPeriod",
                COALESCE(reservation_metrics.active_reservations_in_period, 0) AS "activeReservationsInPeriod",
                COALESCE(reservation_metrics.cancelled_reservations_in_period, 0) AS "cancelledReservationsInPeriod",
                COALESCE(reservation_metrics.reserved_revenue_in_period, 0) AS "reservedRevenueInPeriod",
                COALESCE(reservation_metrics.paid_deposits_count_in_period, 0) AS "paidDepositsCountInPeriod",
                COALESCE(reservation_metrics.paid_deposits_amount_in_period, 0) AS "paidDepositsAmountInPeriod",
                review_metrics.average_rating AS "averageRating"
            FROM accommodations a
            LEFT JOIN (
                SELECT
                    r.accommodation_id,
                    COUNT(*) AS total_reservations_in_period,
                    SUM(CASE WHEN r.status = 'ACTIVE' THEN 1 ELSE 0 END) AS active_reservations_in_period,
                    SUM(CASE WHEN r.status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled_reservations_in_period,
                    COALESCE(SUM(r.total_price), 0) AS reserved_revenue_in_period,
                    SUM(CASE WHEN r.deposit_paid = TRUE THEN 1 ELSE 0 END) AS paid_deposits_count_in_period,
                    COALESCE(SUM(CASE WHEN r.deposit_paid = TRUE THEN r.deposit_amount ELSE 0 END), 0) AS paid_deposits_amount_in_period
                FROM reservations r
                WHERE r.start_date < :periodEndExclusive
                  AND r.end_date >= :periodStart
                GROUP BY r.accommodation_id
            ) reservation_metrics
                ON reservation_metrics.accommodation_id = a.id
            LEFT JOIN (
                SELECT
                    rv.accommodation_id,
                    CAST(AVG(rv.rating) AS DOUBLE PRECISION) AS average_rating
                FROM reviews rv
                GROUP BY rv.accommodation_id
            ) review_metrics
                ON review_metrics.accommodation_id = a.id
            WHERE a.host_id = :hostId
              AND a.deleted = FALSE
            ORDER BY a.title
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM accommodations a
            WHERE a.host_id = :hostId
              AND a.deleted = FALSE
            """,
            nativeQuery = true
    )
    Page<HostAccommodationMetricsProjection> findHostAccommodationMetricsByPeriod(
            @Param("hostId") Long hostId,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEndExclusive") LocalDateTime periodEndExclusive,
            Pageable pageable
    );

    /**
     * Finds an active, available accommodation by its unique identifier.
     *
     * <p>This method combines the soft-delete filter with availability checking,
     * ensuring that the returned accommodation is both not deleted and currently
     * available for booking. It is ideal for search and display operations where
     * only bookable accommodations should be shown.</p>
     *
     * <p><b>Query Conditions:</b></p>
     * <ul>
     *   <li>Accommodation must NOT be soft-deleted ({@code deleted = false})</li>
     *   <li>Accommodation must be available for booking ({@code available = true})</li>
     *   <li>Accommodation must exist with the specified ID</li>
     * </ul>
     *
     * <p><b>Difference from {@link #findByIdAndDeletedFalse(Long)}:</b></p>
     * <ul>
     *   <li>{@code findByIdAndDeletedFalse} only checks soft-delete status</li>
     *   <li>This method additionally checks the availability flag</li>
     * </ul>
     *
     * <p><b>Usage Pattern:</b></p>
     * <pre>{@code
     * // For displaying search results or bookable accommodations
     * Optional<Accommodation> accommodation = accommodationRepository
     *     .findByIdAndDeletedFalseAndAvailableTrue(accommodationId);
     *
     * if (accommodation.isPresent()) {
     *     // Show accommodation details and allow booking
     * } else {
     *     // Show message that accommodation is not available
     * }
     *
     * // For detailed view that includes unavailable but not deleted accommodations
     * // use findByIdAndDeletedFalse instead
     * }</pre>
     *
     * <p><b>Performance:</b> Uses the primary key index plus two boolean checks,
     * making this query very efficient.</p>
     *
     * @param id The unique identifier of the accommodation to find
     * @return An {@code Optional} containing the accommodation if it exists,
     *         is not deleted, and is available; otherwise an empty {@code Optional}
     * @see #findByIdAndDeletedFalse(Long)
     */
    Optional<Accommodation> findByIdAndDeletedFalseAndAvailableTrue(Long id);
}
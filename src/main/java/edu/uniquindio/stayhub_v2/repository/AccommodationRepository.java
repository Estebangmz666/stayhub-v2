package edu.uniquindio.stayhub_v2.repository;

import edu.uniquindio.stayhub_v2.model.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

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
 * <p><b>⚠️ Soft-Delete Consideration:</b></p>
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
 * @author Esteban Gómez León
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

    /*
     * Additional query methods that could be added in the future:
     *
     * // Find all active accommodations
     * List<Accommodation> findAllByDeletedFalse();
     *
     * // Find active accommodations by host
     * List<Accommodation> findByHostIdAndDeletedFalse(Long hostId);
     *
     * // Find active accommodations by city
     * List<Accommodation> findByCityAndDeletedFalse(String city);
     *
     * // Find active accommodations within price range
     * List<Accommodation> findByPricePerNightBetweenAndDeletedFalse(
     *     BigDecimal minPrice, BigDecimal maxPrice);
     *
     * // Find active accommodations by capacity
     * List<Accommodation> findByCapacityGreaterThanEqualAndDeletedFalse(
     *     Integer minCapacity);
     *
     * // Check if active accommodation exists
     * boolean existsByIdAndDeletedFalse(Long id);
     *
     * // Count active accommodations by host
     * long countByHostIdAndDeletedFalse(Long hostId);
     *
     * // Find available accommodations for date range (requires custom @Query)
     * @Query("""
     *     SELECT a FROM Accommodation a
     *     WHERE a.deleted = false
     *     AND a.available = true
     *     AND a.id NOT IN (
     *         SELECT r.accommodation.id FROM Reservation r
     *         WHERE r.status = 'ACTIVE'
     *         AND ((r.startDate <= :endDate) AND (r.endDate >= :startDate))
     *     )
     * """)
     * List<Accommodation> findAvailableAccommodations(
     *     @Param("startDate") LocalDateTime startDate,
     *     @Param("endDate") LocalDateTime endDate);
     */
}
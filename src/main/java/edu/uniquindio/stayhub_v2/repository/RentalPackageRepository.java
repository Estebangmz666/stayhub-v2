package edu.uniquindio.stayhub_v2.repository;

import edu.uniquindio.stayhub_v2.model.RentalPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link RentalPackage} entities.
 *
 * <p>Provides standard CRUD operations inherited from {@link JpaRepository}
 * plus custom queries for listing and overlap detection.</p>
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface RentalPackageRepository extends JpaRepository<RentalPackage, Long> {

    /**
     * Returns all rental packages associated with a given accommodation,
     * ordered by start date ascending.
     *
     * @param accommodationId the accommodation identifier
     * @return ordered list of packages (may be empty)
     */
    List<RentalPackage> findByAccommodationIdOrderByStartDateAsc(Long accommodationId);

    /**
     * Returns a rental package by its own ID and the accommodation it belongs to.
     *
     * <p>Used to confirm ownership before update/delete operations.</p>
     *
     * @param id              the rental package identifier
     * @param accommodationId the accommodation identifier
     * @return an Optional containing the package if found, empty otherwise
     */
    Optional<RentalPackage> findByIdAndAccommodationId(Long id, Long accommodationId);

    /**
     * Checks whether any package for the given accommodation overlaps with
     * the supplied date range, optionally excluding a specific package
     * (useful during updates to avoid self-collision).
     *
     * <p>Two ranges [s1, e1] and [s2, e2] overlap when: {@code s1 <= e2 AND e1 >= s2}.</p>
     *
     * @param accommodationId the accommodation to check
     * @param excludeId       package ID to exclude from the check (-1L for creates)
     * @param startDate       start of the range to test
     * @param endDate         end of the range to test
     * @return {@code true} if at least one overlapping package exists
     */
    @Query("""
            SELECT COUNT(p) > 0
            FROM RentalPackage p
            WHERE p.accommodation.id = :accommodationId
              AND p.id <> :excludeId
              AND p.startDate <= :endDate
              AND p.endDate >= :startDate
            """)
    boolean existsOverlappingPackage(
            @Param("accommodationId") Long accommodationId,
            @Param("excludeId") Long excludeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

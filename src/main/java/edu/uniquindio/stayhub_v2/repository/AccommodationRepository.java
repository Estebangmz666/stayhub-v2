package edu.uniquindio.stayhub_v2.repository;

import edu.uniquindio.stayhub_v2.model.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for Accommodation entities, providing methods for data access and persistence.
 *
 * @author Esteban Gómez León
 * @version 1.0
 */
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
    Optional<Accommodation> findByIdAndDeletedFalse(Long id);
}
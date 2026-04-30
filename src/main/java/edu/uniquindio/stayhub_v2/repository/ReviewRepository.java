package edu.uniquindio.stayhub_v2.repository;

import edu.uniquindio.stayhub_v2.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing guest reviews.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByReservationId(Long reservationId);

    List<Review> findByAccommodationIdOrderByCreatedAtDesc(Long accommodationId);
}
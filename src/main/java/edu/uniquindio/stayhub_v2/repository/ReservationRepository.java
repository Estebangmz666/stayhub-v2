package edu.uniquindio.stayhub_v2.repository;

import edu.uniquindio.stayhub_v2.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import edu.uniquindio.stayhub_v2.model.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByAccommodationIdAndStartDateAfterAndStatus(Long accommodationId, LocalDate date, ReservationStatus status);
}

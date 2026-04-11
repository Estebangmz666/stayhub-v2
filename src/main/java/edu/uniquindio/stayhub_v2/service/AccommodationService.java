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

import java.time.LocalDate;

/**
 * Service class for managing accommodation-related operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;
    private final ReservationRepository reservationRepository;

    public AccommodationGetByIdResponseDTO getAccommodation(Long id){
        Accommodation accommodation = accommodationRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AccommodationNotFoundException("No se encontró ninguna casa con ese código"));
        return accommodationMapper.toAccommodationGetByIdResponseDTO(accommodation);
    }

    @Transactional
    public void deactivateAccommodation(Long id, String requesterEmail) {
        log.info("Request to deactivate accommodation {} by user {}", id, requesterEmail);
        
        Accommodation accommodation = accommodationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AccommodationNotFoundException("No se encontró ninguna casa con ese código"));

        if (!accommodation.getHost().getEmail().equals(requesterEmail)) {
            log.warn("User {} attempted to deactivate accommodation {} without ownership", requesterEmail, id);
            throw new UnauthorizedHostException("No tienes permisos para dar de baja esta casa rural.");
        }

        boolean hasFutureReservations = reservationRepository.existsByAccommodationIdAndStartDateAfterAndStatus(
                id, LocalDate.now(), ReservationStatus.ACTIVE);

        if (hasFutureReservations) {
            log.warn("Cannot deactivate accommodation {} due to active future reservations", id);
            throw new ActiveReservationsException("No se puede dar de baja una casa con reservas futuras");
        }

        accommodation.setDeleted(true);
        accommodation.setAvailable(false);
        accommodationRepository.save(accommodation);
        log.info("Accommodation {} successfully deactivated", id);
    }
}
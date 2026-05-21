package edu.uniquindio.stayhub_v2.controller;

import edu.uniquindio.stayhub_v2.dto.accommodation.AccommodationGetByIdResponseDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.RetrieveReservationResponseDTO;
import edu.uniquindio.stayhub_v2.service.AccommodationService;
import edu.uniquindio.stayhub_v2.service.ReservationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Actions", description = "REST Controller for admin related actions")
@RestController
@RequestMapping("/api/v2/admin-actions")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AccommodationService accommodationService;
    private final ReservationService reservationService;

    @GetMapping("/accommodations/{id}")
    public ResponseEntity<AccommodationGetByIdResponseDTO> getAccommodationById(
            @PathVariable
            @Positive(message = "ID must be positive") Long id
    ) {
        log.info("Retrieving accommodation with ID: {} on Admin Actions", id);
        AccommodationGetByIdResponseDTO accommodationGetByIdResponseDTO = accommodationService.getAccommodation(id);
        log.debug("Accommodation retrieved successfully with title: {}", accommodationGetByIdResponseDTO.title());
        return ResponseEntity.ok(accommodationGetByIdResponseDTO);
    }

    @GetMapping("/reservations/{id}")
    public ResponseEntity<RetrieveReservationResponseDTO> getReservationById(
            @PathVariable
            @Positive(message = "ID must be positive") Long id
    ) {
        log.info("Retrieving reservation with ID: {} on Admin Actions", id);
        RetrieveReservationResponseDTO reservationResponseDTO = reservationService.getReservationByIdOnAdminActions(id);
        log.debug("Reservation retrieved successfully with id: {}", reservationResponseDTO.id());
        return ResponseEntity.ok(reservationResponseDTO);
    }
}

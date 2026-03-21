package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.dto.accommodation.AccommodationGetByIdResponseDTO;
import edu.uniquindio.stayhub_v2.exception.AccommodationNotFoundException;
import edu.uniquindio.stayhub_v2.mapper.AccommodationMapper;
import edu.uniquindio.stayhub_v2.model.Accommodation;
import edu.uniquindio.stayhub_v2.repository.AccommodationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service class for managing accommodation-related operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;

    public AccommodationGetByIdResponseDTO getAccommodation(Long id){
        Accommodation accommodation = accommodationRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AccommodationNotFoundException("Accommodation not found with id: " + id));
        return accommodationMapper.toAccommodationGetByIdResponseDTO(accommodation);
    }
}
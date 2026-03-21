package edu.uniquindio.stayhub_v2.mapper;

import edu.uniquindio.stayhub_v2.dto.accommodation.AccommodationGetByIdResponseDTO;
import edu.uniquindio.stayhub_v2.model.Accommodation;
import org.mapstruct.Mapper;

/**
 * Mapper interface for mapping Accommodation objects.
 *
 * @author Esteban Gómez León
 * @version 1.0
 */
@Mapper(componentModel = "spring")
public interface AccommodationMapper {
    /**
     * Maps an Accommodation entity to an AccommodationGetByIdResponseDTO.
     * @param accommodation the entity to be mapped
     * @return AccommodationGetByIdResponseDTO
     */
    AccommodationGetByIdResponseDTO toAccommodationGetByIdResponseDTO(Accommodation accommodation);
}
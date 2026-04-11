package edu.uniquindio.stayhub_v2.mapper;

import edu.uniquindio.stayhub_v2.dto.accommodation.AccommodationGetByIdResponseDTO;
import edu.uniquindio.stayhub_v2.model.Accommodation;
import org.mapstruct.Mapper;

/**
 * Mapper interface for converting Accommodation entities to DTOs.
 *
 * <p>This mapper uses MapStruct to generate type-safe mapping code at compile time.
 * The {@code componentModel = "spring"} configuration makes the generated
 * implementation a Spring bean that can be injected via {@code @Autowired}.</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * @Service
 * public class AccommodationService {
 *
 *     private final AccommodationMapper accommodationMapper;
 *
 *     public AccommodationGetByIdResponseDTO getAccommodation(Long id) {
 *         Accommodation accommodation = accommodationRepository.findById(id)
 *                 .orElseThrow(() -> new AccommodationNotFoundException("Not found"));
 *         return accommodationMapper.toAccommodationGetByIdResponseDTO(accommodation);
 *     }
 * }
 * }</pre>
 *
 * <p><b>Mapping Strategy:</b></p>
 * Fields with matching names are automatically mapped. Custom mappings
 * can be defined using {@code @Mapping} annotations when field names differ
 * or when nested object traversal is required.</p>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see org.mapstruct.Mapper
 * @see Accommodation
 * @see AccommodationGetByIdResponseDTO
 */
@Mapper(componentModel = "spring")
public interface AccommodationMapper {

    /**
     * Maps an Accommodation entity to an AccommodationGetByIdResponseDTO.
     *
     * <p>This method performs an automatic field-by-field mapping. Fields
     * with identical names in the source and target types are copied
     * automatically. Nested objects are mapped recursively if corresponding
     * mapper methods exist.</p>
     *
     * <p><b>Mapped Fields:</b></p>
     * <ul>
     *   <li>All primitive and String fields with matching names</li>
     *   <li>Date/time fields with matching types</li>
     *   <li>Enumerations with matching names</li>
     * </ul>
     *
     * @param accommodation The Accommodation entity to be mapped (must not be null)
     * @return AccommodationGetByIdResponseDTO containing the mapped data
     */
    AccommodationGetByIdResponseDTO toAccommodationGetByIdResponseDTO(Accommodation accommodation);
}
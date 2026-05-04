package edu.uniquindio.stayhub_v2.mapper;

import edu.uniquindio.stayhub_v2.dto.rental.CreateRentalPackageRequestDTO;
import edu.uniquindio.stayhub_v2.dto.rental.RentalPackageResponseDTO;
import edu.uniquindio.stayhub_v2.model.RentalPackage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between {@link RentalPackage} entities and DTOs.
 *
 * <p>The {@code componentModel = "spring"} configuration makes the generated
 * implementation a Spring bean that can be injected via {@code @Autowired}.</p>
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Mapper(componentModel = "spring")
public interface RentalPackageMapper {

    /**
     * Maps a create-request DTO to a {@link RentalPackage} entity.
     *
     * <p>The {@code id}, {@code accommodation}, {@code createdAt} and
     * {@code updatedAt} fields are intentionally ignored because they are
     * assigned by the service or managed by Spring Data Auditing.</p>
     *
     * @param dto the incoming request
     * @return a partially populated entity (accommodation must be set by the service)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accommodation", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RentalPackage toEntity(CreateRentalPackageRequestDTO dto);

    /**
     * Maps a {@link RentalPackage} entity to a response DTO.
     *
     * @param rentalPackage the persisted entity
     * @return a fully populated response DTO
     */
    @Mapping(target = "accommodationId", source = "accommodation.id")
    RentalPackageResponseDTO toResponseDTO(RentalPackage rentalPackage);
}

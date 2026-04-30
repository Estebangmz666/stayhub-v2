package edu.uniquindio.stayhub_v2.mapper;

import edu.uniquindio.stayhub_v2.dto.review.ReviewResponseDTO;
import edu.uniquindio.stayhub_v2.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for converting Review entities into API response DTOs.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "accommodationId", source = "accommodation.id")
    @Mapping(target = "guestId", source = "guest.id")
    @Mapping(target = "guestName", source = "guest.fullName")
    @Mapping(target = "stayStartDate", source = "reservation.startDate")
    @Mapping(target = "stayEndDate", source = "reservation.endDate")
    ReviewResponseDTO toResponseDTO(Review review);
}

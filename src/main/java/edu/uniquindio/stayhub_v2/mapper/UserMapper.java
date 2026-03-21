package edu.uniquindio.stayhub_v2.mapper;

import edu.uniquindio.stayhub_v2.dto.user.UserSignupRequestDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserSignupResponseDTO;
import edu.uniquindio.stayhub_v2.model.User;
import org.mapstruct.Mapper;

/**
 * Mapper interface for mapping User objects.
 * @author Esteban Gómez León
 * @version 1.0
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    /**
     * Maps a UserSignupRequestDTO to a User entity.
     * @param userSignupRequestDTO the dto to be mapped
     * @return User
     */
    User toEntity(UserSignupRequestDTO userSignupRequestDTO);

    /**
     * Maps a User entity to a UserSignupResponseDTO.
     * @param user the entity to be mapped
     * @return UserSignupResponseDTO
     */
    UserSignupResponseDTO toSignupResponseDTO(User user);
}
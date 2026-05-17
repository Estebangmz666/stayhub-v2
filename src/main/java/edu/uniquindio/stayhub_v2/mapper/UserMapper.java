package edu.uniquindio.stayhub_v2.mapper;

import edu.uniquindio.stayhub_v2.dto.user.UserProfileResponseDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserSignupRequestDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserSignupResponseDTO;
import edu.uniquindio.stayhub_v2.dto.user.profileUpdate.UserProfileUpdateResponseDTO;
import edu.uniquindio.stayhub_v2.model.User;
import org.mapstruct.Mapper;

/**
 * Mapper interface for converting between User entities and DTOs.
 *
 * <p>This mapper uses MapStruct to generate type-safe mapping code at compile time.
 * The {@code componentModel = "spring"} configuration makes the generated
 * implementation a Spring bean that can be injected via {@code @Autowired}.</p>
 *
 * <p>This mapper handles bidirectional mapping:
 * <ul>
 *   <li><b>Inbound:</b> {@code UserSignupRequestDTO} → {@code User} (entity creation)</li>
 *   <li><b>Outbound:</b> {@code User} → {@code UserSignupResponseDTO} (API response)</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * @Service
 * public class UserService {
 *
 *     private final UserMapper userMapper;
 *     private final UserRepository userRepository;
 *     private final PasswordEncoder passwordEncoder;
 *
 *     public UserSignupResponseDTO registerUser(UserSignupRequestDTO request) {
 *         User user = userMapper.toEntity(request);
 *         user.setPassword(passwordEncoder.encode(request.password()));
 *         User savedUser = userRepository.save(user);
 *         return userMapper.toSignupResponseDTO(savedUser);
 *     }
 * }
 * }</pre>
 *
 * <p><b>Security Note:</b></p>
 * The password field is intentionally NOT automatically mapped in the
 * {@code toSignupResponseDTO} method to prevent sensitive data exposure.
 * MapStruct will ignore this field if it doesn't exist in the target DTO.</p>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see org.mapstruct.Mapper
 * @see User
 * @see UserSignupRequestDTO
 * @see UserSignupResponseDTO
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Maps a UserSignupRequestDTO to a User entity.
     *
     * <p>This method is used during user registration to convert the incoming
     * request data into a persistable User entity. Fields are mapped automatically
     * by name.</p>
     *
     * <p><b>Important:</b> The password from the DTO is mapped as plain text.
     * <b>You MUST encode the password before persisting the entity.</b></p>
     *
     * <p><b>Expected Field Mapping:</b></p>
     * <ul>
     *   <li>{@code fullName} → {@code fullName}</li>
     *   <li>{@code email} → {@code email}</li>
     *   <li>{@code password} → {@code password} (plain text, encode before saving!)</li>
     *   <li>{@code phoneNumber} → {@code phoneNumber} (if present)</li>
     *   <li>{@code role} → {@code role} (defaults to GUEST if not specified)</li>
     * </ul>
     *
     * @param userSignupRequestDTO The signup request DTO containing user data
     * @return A new User entity with mapped fields (password in plain text)
     */
    User toEntity(UserSignupRequestDTO userSignupRequestDTO);

    /**
     * Maps a User entity to a UserSignupResponseDTO.
     *
     * <p>This method is used to build the response after successful user
     * registration or user retrieval operations. Only non-sensitive fields
     * are mapped to the response.</p>
     *
     * <p><b>Mapped Fields:</b></p>
     * <ul>
     *   <li>{@code id} → {@code id}</li>
     *   <li>{@code fullName} → {@code fullName}</li>
     *   <li>{@code email} → {@code email}</li>
     *   <li>{@code role} → {@code role}</li>
     *   <li>{@code createdAt} → {@code createdAt} (if present in DTO)</li>
     * </ul>
     *
     * <p><b>Security:</b> The {@code password} field is intentionally excluded
     * from the mapping to prevent sensitive data exposure in API responses.</p>
     *
     * @param user The persisted User entity to be mapped
     * @return UserSignupResponseDTO containing safe, non-sensitive user data
     */
    UserSignupResponseDTO toSignupResponseDTO(User user);

    UserProfileResponseDTO toUserProfileResponseDTO(User user);

    UserProfileUpdateResponseDTO toUserProfileUpdateResponseDTO(User user);
}
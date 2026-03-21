package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.dto.auth.TokenResponseDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserLoginRequestDTO;
import edu.uniquindio.stayhub_v2.exception.EmailAlreadyExistsException;
import edu.uniquindio.stayhub_v2.dto.user.UserSignupRequestDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserSignupResponseDTO;
import edu.uniquindio.stayhub_v2.exception.InvalidPasswordException;
import edu.uniquindio.stayhub_v2.exception.UserNotFoundException;
import edu.uniquindio.stayhub_v2.mapper.UserMapper;
import edu.uniquindio.stayhub_v2.model.User;
import edu.uniquindio.stayhub_v2.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Service class for managing user operations.
 * @author Esteban Gómez León
 * @version 1.0
 */
@Slf4j @RequiredArgsConstructor @Validated @Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    @Transactional
    public UserSignupResponseDTO registerUser(@Valid UserSignupRequestDTO userSignupRequestDTO) {
        if (userRepository.findByEmail(userSignupRequestDTO.email()).isPresent()){
            log.error("Email already exists: {}", userSignupRequestDTO.email());
            throw new EmailAlreadyExistsException("Email already exists");
        }
        log.info("User registered successfully: {}", userSignupRequestDTO.email());

        User user = userMapper.toEntity(userSignupRequestDTO);
        log.debug("User mapped to entity successfully: {}", user.getEmail());

        user.setPassword(passwordEncoder.encode(userSignupRequestDTO.password()));
        log.debug("Password encoded for user: {}", user.getEmail());

        User savedUser = userRepository.save(user);
        log.debug("User saved successfully: {}", savedUser.getEmail());

        return userMapper.toSignupResponseDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public TokenResponseDTO loginUser(@Valid UserLoginRequestDTO userLoginRequestDTO){
        User user = userRepository.findByEmail(userLoginRequestDTO.email()).orElseThrow(() -> new UserNotFoundException("User not found"));
        log.debug("User found with email: {}", user.getEmail());

        if (!passwordEncoder.matches(userLoginRequestDTO.password(), user.getPassword())){
            log.warn("Invalid password attempt for email: {}", user.getEmail());
            throw new InvalidPasswordException("Email or password invalid");
        }
        log.info("User logged in successfully: {}", user.getEmail());

        String token = jwtService.generateToken(user);
        log.debug("JWT token generated for user: {}", user.getEmail());
        return new TokenResponseDTO(token);
    }
}
package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.dto.auth.TokenResponseDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserLoginRequestDTO;
import edu.uniquindio.stayhub_v2.exception.EmailAlreadyExistsException;
import edu.uniquindio.stayhub_v2.dto.user.UserSignupRequestDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserSignupResponseDTO;
import edu.uniquindio.stayhub_v2.exception.InvalidPasswordException;
import edu.uniquindio.stayhub_v2.exception.UserNotFoundException;
import edu.uniquindio.stayhub_v2.dto.auth.ForgotPasswordRequestDTO;
import edu.uniquindio.stayhub_v2.dto.auth.ResetPasswordRequestDTO;
import edu.uniquindio.stayhub_v2.dto.auth.ChangePasswordRequestDTO;
import edu.uniquindio.stayhub_v2.exception.InvalidRecoveryCodeException;
import edu.uniquindio.stayhub_v2.mapper.UserMapper;
import edu.uniquindio.stayhub_v2.model.User;
import edu.uniquindio.stayhub_v2.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.Random;
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
    private final EmailService emailService;

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
            log.warn("Invalid login attempt detected");
            throw new InvalidPasswordException("Email or password invalid");
        }
        log.info("User logged in successfully: {}", user.getEmail());

        String token = jwtService.generateToken(user);
        log.debug("JWT token generated for user: {}", user.getEmail());
        return new TokenResponseDTO(token);
    }

    @Transactional
    public void forgotPassword(@Valid ForgotPasswordRequestDTO requestDTO){
        User user = userRepository.findByEmail(requestDTO.email())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        String code = generateRecoveryCode();
        user.setPasswordRecoveryCode(code);
        user.setPasswordRecoveryExpiration(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        String text = "Hola " + user.getFullName() + ",\n\n" +
                "Has solicitado recuperar tu contraseña. Usa el siguiente código para reestablecerla:\n\n" +
                "Código: " + code + "\n\n" +
                "Este código es válido por 15 minutos.";

        emailService.sendEmail(user.getEmail(), "Recuperación de contraseña", text);
        log.info("Recovery code sent to {}", user.getEmail());
    }

    @Transactional
    public void resetPassword(@Valid ResetPasswordRequestDTO requestDTO){
        User user = userRepository.findByEmail(requestDTO.email())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if(user.getPasswordRecoveryCode() == null || !user.getPasswordRecoveryCode().equals(requestDTO.code())){
            throw new InvalidRecoveryCodeException("El código de recuperación es inválido");
        }

        if(user.getPasswordRecoveryExpiration().isBefore(LocalDateTime.now())){
            throw new InvalidRecoveryCodeException("El código de recuperación ha expirado");
        }

        user.setPassword(passwordEncoder.encode(requestDTO.newPassword()));
        user.setPasswordRecoveryCode(null);
        user.setPasswordRecoveryExpiration(null);

        userRepository.save(user);
        log.info("Password successfully reset for {}", user.getEmail());
    }

    @Transactional
    public void changePassword(String email, @Valid ChangePasswordRequestDTO requestDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(requestDTO.currentPassword(), user.getPassword())) {
            log.warn("Invalid current password attempt for changing password");
            throw new InvalidPasswordException("La contraseña actual es incorrecta");
        }

        user.setPassword(passwordEncoder.encode(requestDTO.newPassword()));
        userRepository.save(user);
        log.info("Password successfully changed for {}", user.getEmail());
    }

    private String generateRecoveryCode(){
        int code = 100000 + new Random().nextInt(900000);
        return String.valueOf(code);
    }
}
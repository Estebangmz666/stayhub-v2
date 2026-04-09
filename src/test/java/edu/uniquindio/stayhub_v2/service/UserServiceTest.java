package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.dto.auth.ForgotPasswordRequestDTO;
import edu.uniquindio.stayhub_v2.dto.auth.ResetPasswordRequestDTO;
import edu.uniquindio.stayhub_v2.dto.auth.TokenResponseDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserLoginRequestDTO;
import edu.uniquindio.stayhub_v2.exception.InvalidPasswordException;
import edu.uniquindio.stayhub_v2.exception.InvalidRecoveryCodeException;
import edu.uniquindio.stayhub_v2.exception.UserNotFoundException;
import edu.uniquindio.stayhub_v2.mapper.UserMapper;
import edu.uniquindio.stayhub_v2.model.User;
import edu.uniquindio.stayhub_v2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JWTService jwtService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@mail.com")
                .password("encoded_password")
                .fullName("Test User")
                .build();
    }

    @Test
    void loginUser_ValidCredentials_ReturnsToken() {
        UserLoginRequestDTO request = new UserLoginRequestDTO("test@mail.com", "Password123!");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.password(), testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn("fake-jwt-token");

        TokenResponseDTO response = userService.loginUser(request);

        assertThat(response.token()).isEqualTo("fake-jwt-token");
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void loginUser_InvalidPassword_ThrowsException() {
        UserLoginRequestDTO request = new UserLoginRequestDTO("test@mail.com", "WrongPassword!");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.password(), testUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.loginUser(request))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void loginUser_UserNotFound_ThrowsException() {
        UserLoginRequestDTO request = new UserLoginRequestDTO("notfound@mail.com", "Password123!");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loginUser(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void forgotPassword_ValidEmail_GeneratesCodeAndSendsEmail() {
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO("test@mail.com");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(testUser));

        userService.forgotPassword(request);

        assertThat(testUser.getPasswordRecoveryCode()).isNotNull();
        assertThat(testUser.getPasswordRecoveryCode()).hasSize(6);
        assertThat(testUser.getPasswordRecoveryExpiration()).isAfter(LocalDateTime.now().minusMinutes(1));
        
        verify(userRepository).save(testUser);
        verify(emailService).sendEmail(eq(testUser.getEmail()), anyString(), anyString());
    }

    @Test
    void resetPassword_ValidCodeAndNotExpired_UpdatesPassword() {
        testUser.setPasswordRecoveryCode("123456");
        testUser.setPasswordRecoveryExpiration(LocalDateTime.now().plusMinutes(10));
        
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("test@mail.com", "123456", "NewPassword123!");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(request.newPassword())).thenReturn("new_encoded_password");

        userService.resetPassword(request);

        assertThat(testUser.getPassword()).isEqualTo("new_encoded_password");
        assertThat(testUser.getPasswordRecoveryCode()).isNull();
        assertThat(testUser.getPasswordRecoveryExpiration()).isNull();
        
        verify(userRepository).save(testUser);
    }

    @Test
    void resetPassword_InvalidCode_ThrowsException() {
        testUser.setPasswordRecoveryCode("123456");
        testUser.setPasswordRecoveryExpiration(LocalDateTime.now().plusMinutes(10));
        
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("test@mail.com", "999999", "NewPassword123!");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.resetPassword(request))
                .isInstanceOf(InvalidRecoveryCodeException.class)
                .hasMessageContaining("inválido");
    }

    @Test
    void resetPassword_ExpiredCode_ThrowsException() {
        testUser.setPasswordRecoveryCode("123456");
        testUser.setPasswordRecoveryExpiration(LocalDateTime.now().minusMinutes(1));
        
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("test@mail.com", "123456", "NewPassword123!");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.resetPassword(request))
                .isInstanceOf(InvalidRecoveryCodeException.class)
                .hasMessageContaining("expirado");
    }
}

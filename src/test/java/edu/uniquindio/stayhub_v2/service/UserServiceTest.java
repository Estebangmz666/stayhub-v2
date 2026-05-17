package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.dto.auth.ChangePasswordRequestDTO;
import edu.uniquindio.stayhub_v2.dto.auth.ForgotPasswordRequestDTO;
import edu.uniquindio.stayhub_v2.dto.auth.ResetPasswordRequestDTO;
import edu.uniquindio.stayhub_v2.dto.auth.TokenResponseDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserLoginRequestDTO;
import edu.uniquindio.stayhub_v2.exception.InvalidPasswordException;
import edu.uniquindio.stayhub_v2.exception.InvalidRecoveryCodeException;
import edu.uniquindio.stayhub_v2.model.User;
import edu.uniquindio.stayhub_v2.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loginUser_ValidCredentials_ReturnsToken() {
        UserLoginRequestDTO request = new UserLoginRequestDTO("test@mail.com", "Password123!");
        when(userRepository.findByEmailAndDeletedFalse(request.email())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.password(), testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn("fake-jwt-token");

        TokenResponseDTO response = userService.loginUser(request);

        assertThat(response.token()).isEqualTo("fake-jwt-token");
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void loginUser_InvalidPassword_ThrowsException() {
        UserLoginRequestDTO request = new UserLoginRequestDTO("test@mail.com", "WrongPassword!");
        when(userRepository.findByEmailAndDeletedFalse(request.email())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.password(), testUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.loginUser(request))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void loginUser_UserNotFound_ThrowsException() {
        UserLoginRequestDTO request = new UserLoginRequestDTO("notfound@mail.com", "Password123!");
        when(userRepository.findByEmailAndDeletedFalse(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loginUser(request))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void loginUser_DeletedUser_ThrowsException() {
        UserLoginRequestDTO request = new UserLoginRequestDTO("test@mail.com", "Password123!");
        when(userRepository.findByEmailAndDeletedFalse(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loginUser(request))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void forgotPassword_ValidEmail_GeneratesCodeAndSendsEmail() {
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO("test@mail.com");
        when(userRepository.findByEmailAndDeletedFalse(request.email())).thenReturn(Optional.of(testUser));

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

        ResetPasswordRequestDTO request =
                new ResetPasswordRequestDTO("test@mail.com", "123456", "NewPassword123!");
        when(userRepository.findByEmailAndDeletedFalse(request.email())).thenReturn(Optional.of(testUser));
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

        ResetPasswordRequestDTO request =
                new ResetPasswordRequestDTO("test@mail.com", "999999", "NewPassword123!");
        when(userRepository.findByEmailAndDeletedFalse(request.email())).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.resetPassword(request))
                .isInstanceOf(InvalidRecoveryCodeException.class)
                .hasMessageContaining("inv");
    }

    @Test
    void resetPassword_ExpiredCode_ThrowsException() {
        testUser.setPasswordRecoveryCode("123456");
        testUser.setPasswordRecoveryExpiration(LocalDateTime.now().minusMinutes(1));

        ResetPasswordRequestDTO request =
                new ResetPasswordRequestDTO("test@mail.com", "123456", "NewPassword123!");
        when(userRepository.findByEmailAndDeletedFalse(request.email())).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.resetPassword(request))
                .isInstanceOf(InvalidRecoveryCodeException.class)
                .hasMessageContaining("expirado");
    }

    @Test
    void changePassword_ValidCurrentPassword_UpdatesPassword() {
        ChangePasswordRequestDTO request =
                new ChangePasswordRequestDTO("OldPassword123!", "NewPassword123!");

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmailAndDeletedFalseWithRoles(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(userRepository.findByEmailAndDeletedFalse(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.currentPassword(), testUser.getPassword()))
                .thenReturn(true);
        when(passwordEncoder.encode(request.newPassword()))
                .thenReturn("new_encoded_password");

        userService.changePassword(request);

        assertThat(testUser.getPassword()).isEqualTo("new_encoded_password");
        verify(userRepository).findByEmailAndDeletedFalseWithRoles(testUser.getEmail());
        verify(userRepository).findByEmailAndDeletedFalse(testUser.getEmail());
        verify(userRepository).save(testUser);
    }

    @Test
    void changePassword_InvalidCurrentPassword_ThrowsException() {
        ChangePasswordRequestDTO request =
                new ChangePasswordRequestDTO("WrongPassword!", "NewPassword123!");

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmailAndDeletedFalseWithRoles(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(userRepository.findByEmailAndDeletedFalse(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.currentPassword(), testUser.getPassword()))
                .thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(request))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("incorrecta");

        verify(userRepository).findByEmailAndDeletedFalseWithRoles(testUser.getEmail());
        verify(userRepository).findByEmailAndDeletedFalse(testUser.getEmail());
        verify(passwordEncoder).matches("WrongPassword!", testUser.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_ValidCurrentPassword_WithUnicodeNewPassword_UpdatesPassword() {
        ChangePasswordRequestDTO request =
                new ChangePasswordRequestDTO("OldPassword123!", "NuevaContraseña01!");

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmailAndDeletedFalseWithRoles(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(userRepository.findByEmailAndDeletedFalse(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.currentPassword(), testUser.getPassword()))
                .thenReturn(true);
        when(passwordEncoder.encode(request.newPassword()))
                .thenReturn("new_encoded_password");

        userService.changePassword(request);

        assertThat(testUser.getPassword()).isEqualTo("new_encoded_password");
        verify(userRepository).findByEmailAndDeletedFalseWithRoles(testUser.getEmail());
        verify(userRepository).findByEmailAndDeletedFalse(testUser.getEmail());
        verify(passwordEncoder).matches("OldPassword123!", "encoded_password");
        verify(passwordEncoder).encode("NuevaContraseña01!");
        verify(userRepository).save(testUser);
    }

    @Test
    void getCurrentUser_DeletedAuthenticatedUser_ThrowsUsernameNotFoundException() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testUser);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmailAndDeletedFalseWithRoles(testUser.getEmail()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser())
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining(testUser.getEmail());
    }

    @Test
    void changePasswordDTO_ValidPasswordWithUnicode_ShouldPassValidation() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        ChangePasswordRequestDTO dto =
                new ChangePasswordRequestDTO("OldPassword123!", "NuevaContraseña01!");

        Set<ConstraintViolation<ChangePasswordRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void changePasswordDTO_InvalidPassword_ShouldFailValidation() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        ChangePasswordRequestDTO dto = new ChangePasswordRequestDTO("OldPassword123!", "password");

        Set<ConstraintViolation<ChangePasswordRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
    }
}

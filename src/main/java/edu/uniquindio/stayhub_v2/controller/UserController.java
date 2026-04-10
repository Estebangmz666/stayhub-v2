package edu.uniquindio.stayhub_v2.controller;

import edu.uniquindio.stayhub_v2.dto.auth.ForgotPasswordRequestDTO;
import edu.uniquindio.stayhub_v2.dto.auth.MessageResponseDTO;
import edu.uniquindio.stayhub_v2.dto.auth.ResetPasswordRequestDTO;
import edu.uniquindio.stayhub_v2.dto.auth.TokenResponseDTO;
import edu.uniquindio.stayhub_v2.dto.auth.ChangePasswordRequestDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserLoginRequestDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserSignupRequestDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserSignupResponseDTO;
import edu.uniquindio.stayhub_v2.service.UserService;
import edu.uniquindio.stayhub_v2.service.JWTService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Management", description = "Endpoints for managing users")
@RestController
@RequestMapping("/api/v2/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final JWTService jwtService;

    @Operation(summary = "Register a new user", description = "Registers a new user with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserSignupResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "User created successfully",
                                    value = """
                                {
                                    "email": "john.doe@example.com",
                                    "password": "P@ssw0rd123",
                                    "roles": ["GUEST", "HOST"],
                                    "fullName": "John Giggity Doe",
                                    "phoneNumber": "+573101234567",
                                    "birthDate": "1990-01-01",
                                    "profilePicture": "https://example.com/profile.jpg"
                                }
                                """))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class),
                            examples = {
                                    @ExampleObject(name = "Invalid email", value = "{\"message\": \"Invalid email format\", \"code\": 400}"),
                                    @ExampleObject(name = "Empty email", value = "{\"message\": \"Email is required\", \"code\": 400}")
                            })),

    })
    @PostMapping("/auth/signup")
    public ResponseEntity<UserSignupResponseDTO> signupUser(@Valid @RequestBody @Parameter(description = "User Signup Details") UserSignupRequestDTO userSignupRequestDTO){
        log.info("Processing user Signup request for email: {}", userSignupRequestDTO.email());
        UserSignupResponseDTO userSignupResponseDTO = userService.registerUser(userSignupRequestDTO);
        log.debug("User registered successfully: {}", userSignupResponseDTO.email());
        return new ResponseEntity<>(userSignupResponseDTO, HttpStatus.CREATED);
    }

    @Operation(summary = "Logs in a user", description = "Logs in a user with the provided credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponseDTO.class),
                            examples = @ExampleObject(
                                value = "{\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"}")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Invalid email or password",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class),
                        examples = @ExampleObject(
                            value = "{\"message\": \"Email or password invalid\", \"code\": 401}")
                )
            )
    })
    @PostMapping("auth/login")
    public ResponseEntity<TokenResponseDTO> loginUser(@Valid @RequestBody @Parameter(description = "User login credentials") UserLoginRequestDTO userLoginRequestDTO){
        log.info("Processing login request for email: {}", userLoginRequestDTO.email());
        TokenResponseDTO tokenResponse = userService.loginUser(userLoginRequestDTO);
        log.debug("User logged in successfully with email: {}", userLoginRequestDTO.email());
        return new ResponseEntity<>(tokenResponse, HttpStatus.OK);
    }

    @Operation(summary = "Forgot password", description = "Generates a recovery code and sends it via email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recovery code sent successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{\\\"message\\\": \\\"Código de recuperación enviado\\\"}")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class)
                    )
            )
    })
    @PostMapping("auth/forgot-password")
    public ResponseEntity<MessageResponseDTO> forgotPassword(@Valid @RequestBody @Parameter(description = "User's email") ForgotPasswordRequestDTO requestDTO){
        log.info("Processing forgot password request for email: {}", requestDTO.email());
        userService.forgotPassword(requestDTO);
        return new ResponseEntity<>(new MessageResponseDTO("Código de recuperación enviado"), HttpStatus.OK);
    }

    @Operation(summary = "Reset password", description = "Resets user password using a recovery code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{\\\"message\\\": \\\"Contraseña restablecida exitosamente\\\"}")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid or expired recovery code",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class)
                    )
            )
    })
    @PostMapping("auth/reset-password")
    public ResponseEntity<MessageResponseDTO> resetPassword(@Valid @RequestBody @Parameter(description = "Reset password details") ResetPasswordRequestDTO requestDTO){
        log.info("Processing reset password request for email: {}", requestDTO.email());
        userService.resetPassword(requestDTO);
        return new ResponseEntity<>(new MessageResponseDTO("Contraseña restablecida exitosamente"), HttpStatus.OK);
    }

    @Operation(summary = "Change password", description = "Changes user's password requiring the current password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{\\\"message\\\": \\\"Contraseña cambiada exitosamente\\\"}")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Validation or rules error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Invalid current password",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class)
                    )
            )
    })
    @PutMapping("auth/change-password")
    public ResponseEntity<MessageResponseDTO> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO requestDTO,
            @RequestHeader("Authorization") String token) {
        log.info("Processing change password request");
        String requesterEmail = jwtService.getEmailFromToken(token);
        userService.changePassword(requesterEmail, requestDTO);
        return new ResponseEntity<>(new MessageResponseDTO("Contraseña cambiada exitosamente"), HttpStatus.OK);
    }
}
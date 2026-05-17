package edu.uniquindio.stayhub_v2.controller;

import edu.uniquindio.stayhub_v2.dto.auth.ForgotPasswordRequestDTO;
import edu.uniquindio.stayhub_v2.dto.auth.MessageResponseDTO;
import edu.uniquindio.stayhub_v2.dto.auth.ResetPasswordRequestDTO;
import edu.uniquindio.stayhub_v2.dto.auth.TokenResponseDTO;
import edu.uniquindio.stayhub_v2.dto.auth.ChangePasswordRequestDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserLoginRequestDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserProfileResponseDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserSignupRequestDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserSignupResponseDTO;
import edu.uniquindio.stayhub_v2.dto.user.profileUpdate.UserProfileUpdateRequestDTO;
import edu.uniquindio.stayhub_v2.dto.user.profileUpdate.UserProfileUpdateResponseDTO;
import edu.uniquindio.stayhub_v2.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Management", description = "Endpoints for managing users")
@RestController
@RequestMapping("/api/v2/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

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
            @Valid @RequestBody ChangePasswordRequestDTO requestDTO) {
        log.info("Processing change password request");
        userService.changePassword(requestDTO);
        return new ResponseEntity<>(new MessageResponseDTO("Password changed successfully"), HttpStatus.OK);
    }

    @Operation(
            summary = "Get authenticated user profile",
            description = """
                Retrieves the profile information of the currently authenticated user.

                This endpoint requires a valid JWT Bearer token. The authenticated user is resolved
                from the Spring Security context, then the corresponding user entity is loaded from
                the database with its roles and mapped to a profile response DTO.

                Authentication flow:
                1. The JWT filter validates the token.
                2. The authenticated principal is stored in the SecurityContext.
                3. The service resolves the current user from the SecurityContext.
                4. The user profile is returned as a UserProfileResponseDTO.

                This endpoint does not receive request parameters or a request body.
                """,
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Authenticated user profile retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserProfileResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Successful response",
                                    summary = "Authenticated user profile",
                                    value = """
                                        {
                                          "id": 1,
                                          "firstName": "John",
                                          "lastName": "Doe",
                                          "email": "john.doe@example.com",
                                          "phoneNumber": "+573001112233",
                                          "roles": [
                                            "ROLE_USER"
                                          ]
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                        Unauthorized. The request does not contain a valid JWT token,
                        the token is missing, expired, malformed, or could not be authenticated.
                        """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized response",
                                    summary = "Missing or invalid token",
                                    value = """
                                        {
                                          "message": "Authentication is required to access this resource",
                                          "code": 401
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = """
                        Forbidden. The user is authenticated but does not have permission
                        to access this resource, or the SecurityContext does not contain
                        a valid authenticated principal.
                        """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class),
                            examples = @ExampleObject(
                                    name = "Forbidden response",
                                    summary = "Access denied",
                                    value = """
                                        {
                                          "message": "User not authenticated",
                                          "code": 403
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                        Not found. The JWT token was valid, but the user referenced by the
                        authenticated principal could not be found in the database.
                        """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class),
                            examples = @ExampleObject(
                                    name = "User not found response",
                                    summary = "Authenticated user does not exist in database",
                                    value = """
                                        {
                                          "message": "User not found with email: john.doe@example.com",
                                          "code": 404
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = """
                        Internal server error. An unexpected error occurred while resolving
                        the authenticated principal or retrieving the user profile.
                        """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class),
                            examples = @ExampleObject(
                                    name = "Internal server error response",
                                    summary = "Unexpected server error",
                                    value = """
                                        {
                                          "message": "Unexpected error while retrieving user profile",
                                          "code": 500
                                        }
                                        """
                            )
                    )
            )
    })
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> getMyProfile() {
        log.info("Processing get my profile request");
        UserProfileResponseDTO userProfileResponseDTO = userService.getMyProfile();
        return ResponseEntity.ok(userProfileResponseDTO);
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfileUpdateResponseDTO> updateMyProfile(
            @Valid @RequestBody UserProfileUpdateRequestDTO userProfileUpdateRequestDTO
    ) {
        log.info("Processing user profile update request");
        UserProfileUpdateResponseDTO userProfileUpdateResponseDTO = userService.updateUserProfile(userProfileUpdateRequestDTO);
        return ResponseEntity.ok(userProfileUpdateResponseDTO);
    }

    @DeleteMapping("/me")
    public ResponseEntity<MessageResponseDTO> deleteMyAccount(){
            log.info("Processing delete my account request");
            userService.softDeleteCurrentUser();
            return ResponseEntity.ok(new MessageResponseDTO("Your account has been deleted successfully. Hope to see you again soon!"));
    }
}
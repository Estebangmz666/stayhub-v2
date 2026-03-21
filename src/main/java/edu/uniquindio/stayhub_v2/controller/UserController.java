package edu.uniquindio.stayhub_v2.controller;

import edu.uniquindio.stayhub_v2.dto.auth.TokenResponseDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserLoginRequestDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserSignupRequestDTO;
import edu.uniquindio.stayhub_v2.dto.user.UserSignupResponseDTO;
import edu.uniquindio.stayhub_v2.service.UserService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserSignupResponseDTO.class),
                            examples = @ExampleObject(value = "{\"id\": 1, \"email\": \"john.doe@example.com\", \"name\": \"John Doe\", \"phoneNumber\": \"+573101234567\", \"birthDate\": \"1995-08-20\", \"role\": \"GUEST\", \"profilePicture\": null, \"description\": null, \"legalDocuments\": null}"))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class),
                            examples = @ExampleObject(value = "{\"message\": \"Invalid email format\", \"code\": 400}"))),
            @ApiResponse(responseCode = "409", description = "Email already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = edu.uniquindio.stayhub_v2.dto.auth.Error.class),
                            examples = @ExampleObject(value = "{\"message\": \"Email already exists\", \"code\": 409}")))
    })
    @PostMapping("/auth/signup")
    public ResponseEntity<UserSignupResponseDTO> signupUser(@Valid @RequestBody @Parameter(description = "User Signup Details") UserSignupRequestDTO userSignupRequestDTO){
        log.info("Processing user Signup request for email: {}", userSignupRequestDTO.email());
        UserSignupResponseDTO userSignupResponseDTO = userService.registerUser(userSignupRequestDTO);
        log.debug("User registered successfully: {}", userSignupResponseDTO.email());
        return new ResponseEntity<>(userSignupResponseDTO, HttpStatus.CREATED);
    }

    @PostMapping("auth/login")
    public ResponseEntity<TokenResponseDTO> loginUser(@Valid @RequestBody @Parameter(description = "User login credentials") UserLoginRequestDTO userLoginRequestDTO){
        log.info("Processing login request for email: {}", userLoginRequestDTO.email());
        TokenResponseDTO tokenResponse = userService.loginUser(userLoginRequestDTO);
        log.debug("User logged in successfully: {}", userLoginRequestDTO.email());
        return new ResponseEntity<>(tokenResponse, HttpStatus.OK);
    }
}
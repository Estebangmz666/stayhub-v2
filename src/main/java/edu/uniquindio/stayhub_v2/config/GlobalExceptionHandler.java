package edu.uniquindio.stayhub_v2.config;

import edu.uniquindio.stayhub_v2.dto.auth.Error;
import edu.uniquindio.stayhub_v2.exception.AccommodationNotFoundException;
import edu.uniquindio.stayhub_v2.exception.InvalidPasswordException;
import edu.uniquindio.stayhub_v2.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice @Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({InvalidPasswordException.class, UserNotFoundException.class})
    public ResponseEntity<Error> handleAuthExceptions(RuntimeException e) {
        log.warn("Invalid login attempt detected");
        return new ResponseEntity<>(
                new Error("Invalid login attempt detected", HttpStatus.UNAUTHORIZED.value()),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(edu.uniquindio.stayhub_v2.exception.InvalidRecoveryCodeException.class)
    public ResponseEntity<Error> handleInvalidRecoveryCodeException(edu.uniquindio.stayhub_v2.exception.InvalidRecoveryCodeException e){
        log.warn("Invalid recovery code: {}", e.getMessage());
        return new ResponseEntity<>(
                new Error(e.getMessage(), HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(edu.uniquindio.stayhub_v2.exception.UnauthorizedHostException.class)
    public ResponseEntity<Error> handleUnauthorizedHostException(edu.uniquindio.stayhub_v2.exception.UnauthorizedHostException e){
        log.warn("Unauthorized host action: {}", e.getMessage());
        return new ResponseEntity<>(
                new Error(e.getMessage(), HttpStatus.FORBIDDEN.value()),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(edu.uniquindio.stayhub_v2.exception.ActiveReservationsException.class)
    public ResponseEntity<Error> handleActiveReservationsException(edu.uniquindio.stayhub_v2.exception.ActiveReservationsException e){
        log.warn("Active reservations prevent action: {}", e.getMessage());
        return new ResponseEntity<>(
                new Error(e.getMessage(), HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(AccommodationNotFoundException.class)
    public ResponseEntity<Error> handleAccommodationNotFoundException(AccommodationNotFoundException e){
        log.warn("Accommodation not found");
        return new ResponseEntity<>(
                new Error("Accommodation not found", HttpStatus.NOT_FOUND.value()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Error> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){

        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("Validation error");

        return ResponseEntity.badRequest()
                .body(new Error(message, 400));
    }
}
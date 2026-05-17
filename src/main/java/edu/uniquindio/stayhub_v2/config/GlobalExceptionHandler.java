package edu.uniquindio.stayhub_v2.config;

import edu.uniquindio.stayhub_v2.dto.auth.Error;
import edu.uniquindio.stayhub_v2.exception.*;
import edu.uniquindio.stayhub_v2.exception.RentalPackageNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

/**
 * Global exception handler for REST controllers.
 *
 * <p>This class centralizes exception handling across all controllers,
 * ensuring consistent error responses and proper HTTP status codes.
 * It intercepts exceptions thrown during request processing and transforms
 * them into standardized {@link Error} responses.</p>
 *
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *   <li>Map domain-specific exceptions to appropriate HTTP status codes</li>
 *   <li>Prevent stack traces from leaking to API consumers</li>
 *   <li>Log errors for monitoring and debugging purposes</li>
 *   <li>Provide consistent error message formatting</li>
 *   <li>Handle validation errors from {@code @Valid} annotations</li>
 * </ul>
 *
 * <p><b>Error Response Structure:</b></p>
 * All error responses follow the {@link Error} DTO structure containing:
 * <ul>
 *   <li>{@code message} - Human-readable error description</li>
 *   <li>{@code status} - HTTP status code</li>
 * </ul>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see RestControllerAdvice
 * @see ExceptionHandler
 * @see Error
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Error> handleInvalidPasswordException(InvalidPasswordException e) {
        log.warn("Authentication error: {}", e.getMessage());
        return new ResponseEntity<>(
                new Error("Invalid login attempt detected", HttpStatus.UNAUTHORIZED.value()),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Error> handleUserNotFoundException(UserNotFoundException e) {
        log.warn("User not found: {}", e.getMessage());
        return new ResponseEntity<>(
                new Error(e.getMessage(), HttpStatus.NOT_FOUND.value()),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Handles exceptions related to invalid password recovery codes.
     *
     * <p>Triggered when a user attempts to use an expired, already used,
     * or non-existent recovery code for password reset.</p>
     *
     * @param e The invalid recovery code exception
     * @return ResponseEntity with BAD_REQUEST status and specific error message
     */
    @ExceptionHandler(InvalidRecoveryCodeException.class)
    public ResponseEntity<Error> handleInvalidRecoveryCodeException(InvalidRecoveryCodeException e) {
        log.warn("Invalid recovery code: {}", e.getMessage());
        return new ResponseEntity<>(
                new Error(e.getMessage(), HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handles unauthorized host actions.
     *
     * <p>Triggered when a host attempts to perform an action on an accommodation
     * or resource that does not belong to them. This ensures hosts can only
     * manage their own properties.</p>
     *
     * @param e The unauthorized host exception
     * @return ResponseEntity with FORBIDDEN status and specific error message
     */
    @ExceptionHandler(UnauthorizedHostException.class)
    public ResponseEntity<Error> handleUnauthorizedHostException(UnauthorizedHostException e) {
        log.warn("Unauthorized host action: {}", e.getMessage());
        return new ResponseEntity<>(
                new Error(e.getMessage(), HttpStatus.FORBIDDEN.value()),
                HttpStatus.FORBIDDEN
        );
    }

    /**
     * Handles exceptions when active reservations prevent an action.
     *
     * <p>Triggered when attempting to delete or modify an accommodation
     * that still has active (future or ongoing) reservations. This protects
     * guests with existing bookings from disruption.</p>
     *
     * @param e The active reservations exception
     * @return ResponseEntity with BAD_REQUEST status and specific error message
     */
    @ExceptionHandler(ActiveReservationsException.class)
    public ResponseEntity<Error> handleActiveReservationsException(ActiveReservationsException e) {
        log.warn("Active reservations prevent action: {}", e.getMessage());
        return new ResponseEntity<>(
                new Error(e.getMessage(), HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handles exceptions when accommodation is not found.
     *
     * <p>Triggered when attempting to access, update, or delete accommodation
     * that does not exist in the database.</p>
     *
     * @param e The accommodation didn't find exception
     * @return ResponseEntity with NOT_FOUND status and specific error message
     */
    @ExceptionHandler(AccommodationNotFoundException.class)
    public ResponseEntity<Error> handleAccommodationNotFoundException(AccommodationNotFoundException e) {
        log.warn("Accommodation not found");
        return new ResponseEntity<>(
                new Error(e.getMessage(), HttpStatus.NOT_FOUND.value()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(AccommodationAlreadyBookedException.class)
    public ResponseEntity<Error> handleAccommodationAlreadyBookedException(AccommodationAlreadyBookedException e) {
        log.warn("Accommodation already booked: {}", e.getMessage());
        return new ResponseEntity<>(
                new Error(e.getMessage(), HttpStatus.CONFLICT.value()),
                HttpStatus.CONFLICT
        );
    }

    /**
     * Handles validation exceptions from {@code @Valid} annotated request bodies.
     *
     * <p>Triggered when a request body fails Bean Validation constraints.
     * Extracts the first validation error message to return to the client.</p>
     *
     * @param e The method argument validation exception
     * @return ResponseEntity with BAD_REQUEST status and validation error message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Error> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Validation error");

        return ResponseEntity.badRequest()
                .body(new Error(message, 400));
    }

    /**
     * Handles business rule violations.
     *
     * <p>Catches {@link IllegalStateException} thrown when a business rule
     * is violated, such as attempting to book an unavailable date range or
     * performing an operation in an invalid state.</p>
     *
     * @param e The illegal state exception
     * @return ResponseEntity with CONFLICT status and specific error message
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Error> handleIllegalStateException(IllegalStateException e) {
        log.warn("Business rule violation: {}", e.getMessage());
        return new ResponseEntity<>(
                new Error(e.getMessage(), HttpStatus.CONFLICT.value()),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(DepositNotPaidException.class)
    public ResponseEntity<Error> handleDepositNotPaidException(DepositNotPaidException e) {
        log.warn("Deposit payment required: {}", e.getMessage());
        return new ResponseEntity<>(
                new Error(e.getMessage(), HttpStatus.CONFLICT.value()),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(ReservationPolicyViolationException.class)
    public ResponseEntity<Error> handleReservationPolicyViolationException(
            ReservationPolicyViolationException e) {
        log.warn("Reservation policy violation: {}", e.getMessage());
        return new ResponseEntity<>(
                new Error(e.getMessage(), HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handles invalid argument exceptions.
     *
     * <p>Catches {@link IllegalArgumentException} thrown when a method receives
     * an argument with an invalid format or value. This serves as a catch-all
     * for invalid input scenarios not covered by specific exception handlers.</p>
     *
     * @param e The illegal argument exception
     * @return ResponseEntity with BAD_REQUEST status and specific error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Error> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Invalid argument: {}", e.getMessage());
        return new ResponseEntity<>(
                new Error(e.getMessage(), HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handles any unhandled exceptions as a fallback mechanism.
     *
     * <p>This handler prevents stack traces from leaking to API consumers
     * when unexpected errors occur. All unhandled exceptions are logged
     * with full stack trace for debugging purposes.</p>
     *
     * @param e The unhandled exception
     * @return ResponseEntity with INTERNAL_SERVER_ERROR status and generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Error> handleGenericException(Exception e) {
        log.error("Unexpected error occurred: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new Error("An unexpected error occurred. Please try again later.",
                        HttpStatus.INTERNAL_SERVER_ERROR.value()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<Error> handleReservationNotFoundException(ReservationNotFoundException e) {
        log.warn("Reservation not found: {}", e.getMessage());
        return new ResponseEntity<>(
                new Error(e.getMessage(), HttpStatus.NOT_FOUND.value()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(RentalPackageNotFoundException.class)
    public ResponseEntity<Error> handleRentalPackageNotFoundException(RentalPackageNotFoundException e) {
        log.warn("Rental package not found: {}", e.getMessage());
        return new ResponseEntity<>(
                new Error(e.getMessage(), HttpStatus.NOT_FOUND.value()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<Error> handleReviewNotFoundException(ReviewNotFoundException e) {
        log.warn("Review not found: {}", e.getMessage());
        return new ResponseEntity<>(
                new Error(e.getMessage(), HttpStatus.NOT_FOUND.value()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(ReviewPolicyViolationException.class)
    public ResponseEntity<Error> handleReviewPolicyViolationException(ReviewPolicyViolationException e) {
        log.warn("Review policy violation: {}", e.getMessage());
        return new ResponseEntity<>(
                new Error(e.getMessage(), HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Error> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return new ResponseEntity<>(
                new Error(e.getMessage(), HttpStatus.FORBIDDEN.value()),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(org.springframework.security.core.userdetails.UsernameNotFoundException.class)
    public ResponseEntity<Error> handleUsernameNotFoundException(
            org.springframework.security.core.userdetails.UsernameNotFoundException e) {
        log.warn("Authenticated user not found in database: {}", e.getMessage());
        return new ResponseEntity<>(
                new Error(e.getMessage(), HttpStatus.UNAUTHORIZED.value()),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Error> handleConstraintViolationException(
            jakarta.validation.ConstraintViolationException e) {

        String message = e.getConstraintViolations()
                .stream()
                .map(jakarta.validation.ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Validation error");

        log.warn("Constraint violation: {}", message);

        return ResponseEntity.badRequest()
                .body(new Error(message, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Error> handleMethodArgumentTypeMismatchException(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException e) {

        String message = "Invalid value for parameter '" + e.getName() + "'";
        log.warn("Argument type mismatch: {}", e.getMessage());

        return ResponseEntity.badRequest()
                .body(new Error(message, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Error> handleHttpMessageNotReadableException(
            org.springframework.http.converter.HttpMessageNotReadableException e) {

        log.warn("Malformed JSON request: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(new Error("Malformed request body", HttpStatus.BAD_REQUEST.value()));
    }
}

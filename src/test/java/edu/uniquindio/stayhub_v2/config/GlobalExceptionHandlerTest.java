package edu.uniquindio.stayhub_v2.config;

import edu.uniquindio.stayhub_v2.dto.auth.Error;
import edu.uniquindio.stayhub_v2.exception.UnauthorizedHostException;
import edu.uniquindio.stayhub_v2.exception.UnauthorizedRoleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleUnauthorizedRoleException_ReturnsForbiddenWithGenericMessage() {
        UnauthorizedRoleException exception =
                new UnauthorizedRoleException("Only users with the HOST role can perform this action.");

        ResponseEntity<Error> response = globalExceptionHandler.handleUnauthorizedRoleException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("You do not have permission to perform this action.");
        assertThat(response.getBody().code()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void handleUnauthorizedHostException_ReturnsForbiddenWithExceptionMessage() {
        UnauthorizedHostException exception =
                new UnauthorizedHostException("You do not have permissions to deactivate this rural house.");

        ResponseEntity<Error> response = globalExceptionHandler.handleUnauthorizedHostException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo("You do not have permissions to deactivate this rural house.");
        assertThat(response.getBody().code()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void handleAccessDeniedException_ReturnsForbiddenWithExceptionMessage() {
        AccessDeniedException exception =
                new AccessDeniedException("Only the guest who completed the stay can create a review");

        ResponseEntity<Error> response = globalExceptionHandler.handleAccessDeniedException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo("Only the guest who completed the stay can create a review");
        assertThat(response.getBody().code()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }
}

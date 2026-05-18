package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.exception.UnauthorizedRoleException;
import edu.uniquindio.stayhub_v2.model.Role;
import edu.uniquindio.stayhub_v2.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthorizationServiceTest {

    private AuthorizationService authorizationService;
    private User hostUser;
    private User guestUser;
    private User dualRoleUser;
    private User userWithoutRoles;

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService();

        hostUser = User.builder()
                .id(1L)
                .email("host@example.com")
                .roles(Set.of(Role.HOST))
                .build();

        guestUser = User.builder()
                .id(2L)
                .email("guest@example.com")
                .roles(Set.of(Role.GUEST))
                .build();

        dualRoleUser = User.builder()
                .id(3L)
                .email("dual@example.com")
                .roles(Set.of(Role.HOST, Role.GUEST))
                .build();

        userWithoutRoles = User.builder()
                .id(4L)
                .email("noroles@example.com")
                .build();
    }

    @Test
    void hasRole_WhenUserHasRole_ReturnsTrue() {
        boolean result = authorizationService.hasRole(hostUser, Role.HOST);

        assertThat(result).isTrue();
    }

    @Test
    void hasRole_WhenUserDoesNotHaveRole_ReturnsFalse() {
        boolean result = authorizationService.hasRole(guestUser, Role.HOST);

        assertThat(result).isFalse();
    }

    @Test
    void hasRole_WhenUserIsNull_ReturnsFalse() {
        boolean result = authorizationService.hasRole(null, Role.HOST);

        assertThat(result).isFalse();
    }

    @Test
    void hasRole_WhenUserRolesAreNull_ReturnsFalse() {
        boolean result = authorizationService.hasRole(userWithoutRoles, Role.HOST);

        assertThat(result).isFalse();
    }

    @Test
    void hasAnyRole_WhenUserHasOneOfTheRoles_ReturnsTrue() {
        boolean result = authorizationService.hasAnyRole(dualRoleUser, Role.HOST, Role.GUEST);

        assertThat(result).isTrue();
    }

    @Test
    void hasAnyRole_WhenUserHasNoneOfTheRoles_ReturnsFalse() {
        boolean result = authorizationService.hasAnyRole(guestUser, Role.HOST);

        assertThat(result).isFalse();
    }

    @Test
    void hasAnyRole_WhenRolesArrayIsNull_ReturnsFalse() {
        boolean result = authorizationService.hasAnyRole(hostUser, (Role[]) null);

        assertThat(result).isFalse();
    }

    @Test
    void requireRole_WhenUserHasRequiredRole_DoesNotThrow() {
        assertThatCode(() ->
                authorizationService.requireRole(hostUser, Role.HOST, "Host role required")
        ).doesNotThrowAnyException();
    }

    @Test
    void requireRole_WhenUserLacksRequiredRole_ThrowsUnauthorizedRoleException() {
        assertThatThrownBy(() ->
                authorizationService.requireRole(guestUser, Role.HOST, "Host role required")
        )
                .isInstanceOf(UnauthorizedRoleException.class)
                .hasMessage("Host role required");
    }

    @Test
    void requireRole_WhenUserIsNull_ThrowsUnauthorizedRoleException() {
        assertThatThrownBy(() ->
                authorizationService.requireRole(null, Role.HOST, "Host role required")
        )
                .isInstanceOf(UnauthorizedRoleException.class)
                .hasMessage("Host role required");
    }

    @Test
    void requireAnyRole_WhenUserHasAnyRequiredRole_DoesNotThrow() {
        assertThatCode(() ->
                authorizationService.requireAnyRole(dualRoleUser, "Host or guest role required", Role.HOST, Role.GUEST)
        ).doesNotThrowAnyException();
    }

    @Test
    void requireAnyRole_WhenUserLacksAllRequiredRoles_ThrowsUnauthorizedRoleException() {
        assertThatThrownBy(() ->
                authorizationService.requireAnyRole(guestUser, "Host role required", Role.HOST)
        )
                .isInstanceOf(UnauthorizedRoleException.class)
                .hasMessage("Host role required");
    }

    @Test
    void requireHostRole_WhenUserIsHost_DoesNotThrow() {
        assertThatCode(() ->
                authorizationService.requireHostRole(hostUser)
        ).doesNotThrowAnyException();
    }

    @Test
    void requireHostRole_WhenUserIsNotHost_ThrowsUnauthorizedRoleException() {
        assertThatThrownBy(() ->
                authorizationService.requireHostRole(guestUser)
        )
                .isInstanceOf(UnauthorizedRoleException.class)
                .hasMessage("Only users with the HOST role can perform this action.");
    }

    @Test
    void requireGuestRole_WhenUserIsGuest_DoesNotThrow() {
        assertThatCode(() ->
                authorizationService.requireGuestRole(guestUser)
        ).doesNotThrowAnyException();
    }

    @Test
    void requireGuestRole_WhenUserIsNotGuest_ThrowsUnauthorizedRoleException() {
        assertThatThrownBy(() ->
                authorizationService.requireGuestRole(hostUser)
        )
                .isInstanceOf(UnauthorizedRoleException.class)
                .hasMessage("Only users with the GUEST role can perform this action.");
    }
}

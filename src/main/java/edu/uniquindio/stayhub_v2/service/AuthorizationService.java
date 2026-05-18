package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.exception.UnauthorizedRoleException;
import edu.uniquindio.stayhub_v2.model.Role;
import edu.uniquindio.stayhub_v2.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

    private static final String DEFAULT_HOST_REQUIRED_MESSAGE =
            "Only users with the HOST role can perform this action.";

    private static final String DEFAULT_GUEST_REQUIRED_MESSAGE =
            "Only users with the GUEST role can perform this action.";

    public boolean hasRole(User user, Role role) {
        return user != null
                && user.getRoles() != null
                && user.getRoles().contains(role);
    }

    public boolean hasAnyRole(User user, Role... roles) {
        if (user == null || user.getRoles() == null || roles == null) {
            return false;
        }

        for (Role role : roles) {
            if (user.getRoles().contains(role)) {
                return true;
            }
        }
        return false;
    }

    public void requireRole(User user, Role role, String message) {
        if (!hasRole(user, role)) {
            Long userId = user != null ? user.getId() : null;
            log.warn("Access denied for user {}. Required role: {}", userId, role);
            throw new UnauthorizedRoleException(message);
        }
    }

    public void requireAnyRole(User user, String message, Role... roles) {
        if (!hasAnyRole(user, roles)) {
            throw new UnauthorizedRoleException(message);
        }
    }

    public void requireHostRole(User user) {
        requireRole(user, Role.HOST, DEFAULT_HOST_REQUIRED_MESSAGE);
    }

    public void requireGuestRole(User user) {
        requireRole(user, Role.GUEST, DEFAULT_GUEST_REQUIRED_MESSAGE);
    }
}

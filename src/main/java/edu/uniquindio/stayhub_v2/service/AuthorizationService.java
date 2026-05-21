package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.exception.UnauthorizedRoleException;
import edu.uniquindio.stayhub_v2.model.Role;
import edu.uniquindio.stayhub_v2.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * Service responsible for validating role-based access rules within the application.
 *
 * <p>
 * This service centralizes authorization checks related to user roles. It provides
 * specific methods for common access rules, such as requiring a HOST, GUEST, ADMIN,
 * or multiple accepted roles.
 * </p>
 *
 * <p>
 * If a user does not meet the required role condition, an
 * {@link UnauthorizedRoleException} is thrown. This exception should be mapped
 * to an HTTP 403 Forbidden response by the global exception handler.
 * </p>
 *
 * <p>
 * This service is intended to be used from the service layer when business rules
 * require explicit role validation beyond standard endpoint security.
 * </p>
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

    /**
     * Default error message used when a HOST role is required.
     */
    private static final String DEFAULT_HOST_REQUIRED_MESSAGE =
            "Only users with the HOST role can perform this action.";

    /**
     * Default error message used when a GUEST role is required.
     */
    private static final String DEFAULT_GUEST_REQUIRED_MESSAGE =
            "Only users with the GUEST role can perform this action.";

    /**
     * Default error message used when an ADMIN role is required.
     */
    private static final String DEFAULT_ADMIN_REQUIRED_MESSAGE =
            "Only users with the ADMIN role can perform this action.";

//    /**
//     * Default error message used when either a HOST or ADMIN role is required.
//     */
//    private static final String DEFAULT_HOST_OR_ADMIN_REQUIRED_MESSAGE =
//            "Only users with the HOST or ADMIN role can perform this action.";

    /**
     * Requires the given user to have the HOST role.
     *
     * @param user the user whose role will be validated
     * @throws UnauthorizedRoleException if the user does not have the HOST role
     */
    public void requireHostRole(User user) {
        requireRole(user, Role.HOST, DEFAULT_HOST_REQUIRED_MESSAGE);
    }

    /**
     * Requires the given user to have the GUEST role.
     *
     * @param user the user whose role will be validated
     * @throws UnauthorizedRoleException if the user does not have the GUEST role
     */
    public void requireGuestRole(User user) {
        requireRole(user, Role.GUEST, DEFAULT_GUEST_REQUIRED_MESSAGE);
    }

    /**
     * Requires the given user to have the ADMIN role.
     *
     * @param user the user whose role will be validated
     * @throws UnauthorizedRoleException if the user does not have the ADMIN role
     */
    public void requireAdminRole(User user) {
        requireRole(user, Role.ADMIN, DEFAULT_ADMIN_REQUIRED_MESSAGE);
    }

//    /**
//     * Requires the given user to have either the HOST or ADMIN role.
//     *
//     * @param user the user whose roles will be validated
//     * @throws UnauthorizedRoleException if the user does not have either HOST or ADMIN role
//     */
//    public void requireHostOrAdminRole(User user){
//        requireAnyRole(user, DEFAULT_HOST_OR_ADMIN_REQUIRED_MESSAGE, Role.HOST, Role.ADMIN);
//    }

    /**
     * Determines whether the given user can access host reservation features.
     *
     * <p>
     * Currently, only users with the HOST role can access host reservations.
     * This method exists as a semantic authorization rule, making the business
     * intention clearer than directly calling {@link #hasRole(User, Role)}.
     * </p>
     *
     * @param user the user to evaluate
     * @return {@code true} if the user has the HOST role; otherwise {@code false}
     */
    public boolean canAccessHostReservations(User user) {
        return hasRole(user, Role.HOST);
    }

    // ==== HELPERS, DO NOT USE ELSEWHERE EXCEPT TESTS ====

    /**
     * Checks whether the given user has at least one of the provided roles.
     *
     * <p>
     * This method is null-safe. If the user, the user's role collection,
     * or the provided roles are {@code null}, the method returns {@code false}.
     * </p>
     *
     * @param user the user to evaluate
     * @param roles the accepted roles
     * @return {@code true} if the user has at least one of the provided roles;
     * otherwise {@code false}
     */
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

    /**
     * Checks whether the given user has a specific role.
     *
     * <p>
     * This method is null-safe. If the user or the user's role collection
     * is {@code null}, the method returns {@code false}.
     * </p>
     *
     * @param user the user to evaluate
     * @param role the required role
     * @return {@code true} if the user has the required role; otherwise {@code false}
     */
    public boolean hasRole(User user, Role role) {
        return user != null
                && user.getRoles() != null
                && user.getRoles().contains(role);
    }

    /**
     * Requires the given user to have a specific role.
     *
     * <p>
     * If the user does not have the required role, the access attempt is logged
     * and an {@link UnauthorizedRoleException} is thrown with the provided message.
     * </p>
     *
     * @param user the user whose role will be validated
     * @param role the required role
     * @param message the exception message used when access is denied
     * @throws UnauthorizedRoleException if the user does not have the required role
     */
    public void requireRole(User user, Role role, String message) {
        if (!hasRole(user, role)) {
            Long userId = user != null ? user.getId() : null;
            log.warn("Access denied for user {}. Required role: {}", userId, role);
            throw new UnauthorizedRoleException(message);
        }
    }

    /**
     * Requires the given user to have at least one of the provided roles.
     *
     * <p>
     * If the user does not have any of the required roles, the access attempt
     * is logged and an {@link UnauthorizedRoleException} is thrown with the
     * provided message.
     * </p>
     *
     * @param user the user whose roles will be validated
     * @param message the exception message used when access is denied
     * @param roles the accepted roles
     * @throws UnauthorizedRoleException if the user does not have any of the required roles
     */
    public void requireAnyRole(User user, String message, Role... roles) {
        if (!hasAnyRole(user, roles)) {
            Long userId = user != null ? user.getId() : null;
            log.warn("Access denied for user {}. Required roles: {}", userId, roles);
            throw new UnauthorizedRoleException(message);
        }
    }
}
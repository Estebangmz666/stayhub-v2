package edu.uniquindio.stayhub_v2.model;

/**
 * Defines the user roles available in the StayHub platform.
 *
 * <p>
 * Each role represents a different level of access within the system.
 * Roles are mainly used to control which actions a user can perform,
 * such as creating reservations, managing accommodations, or accessing
 * administrative features.
 * </p>
 *
 * <p>
 * This enum should be stored using {@code EnumType.STRING} to avoid
 * persistence issues if the order of the constants changes in the future.
 * </p>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see User
 */
public enum Role {

    /**
     * Represents a guest user.
     *
     * <p>
     * Guests are users who can search accommodations, create reservations,
     * manage their own bookings, and review completed stays.
     * </p>
     *
     * <p>
     * This role is intended for users who consume the platform services
     * as travelers or customers.
     * </p>
     */
    GUEST,

    /**
     * Represents a host user.
     *
     * <p>
     * Hosts are users who can create, publish, and manage accommodations.
     * They can also view reservations associated with their properties
     * and manage availability, pricing, and listing information.
     * </p>
     *
     * <p>
     * This role is intended for property owners or managers who offer
     * accommodations through the platform.
     * </p>
     */
    HOST,

    /**
     * Represents an administrator user.
     *
     * <p>
     * Administrators have access to platform management features.
     * This role is intended for internal users responsible for supervising
     * users, accommodations, reservations, and general system operations.
     * </p>
     */
    ADMIN
}
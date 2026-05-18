package edu.uniquindio.stayhub_v2.model;

/**
 * Enumeration defining the available user roles within the StayHub platform.
 *
 * <p>Roles determine the permissions and capabilities available to a user.
 * The platform follows a dual-role model where users can be either guests
 * (travelers booking accommodations) or hosts (property owners listing
 * accommodations).</p>
 *
 * <p><b>Role-Based Access Control:</b></p>
 * <ul>
 *   <li>Each user is assigned exactly one primary role</li>
 *   <li>Role determines which endpoints and features are accessible</li>
 *   <li>Future enhancement: Users could have multiple roles simultaneously</li>
 * </ul>
 *
 * <p><b>Permission Matrix:</b></p>
 * <table border="1">
 *   <tr><th>Feature</th><th>GUEST</th><th>HOST</th></tr>
 *   <tr><td>Search accommodations</td><td>✅</td><td>✅</td></tr>
 *   <tr><td>Book accommodations</td><td>✅</td><td>❌*</td></tr>
 *   <tr><td>List accommodations</td><td>❌</td><td>✅</td></tr>
 *   <tr><td>Manage own listings</td><td>❌</td><td>✅</td></tr>
 *   <tr><td>View own reservations</td><td>✅</td><td>✅</td></tr>
 *   <tr><td>Leave reviews</td><td>✅</td><td>✅</td></tr>
 * </table>
 * <p><i>*Hosts cannot book their own accommodations</i></p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * // Setting role during registration
 * User newUser = User.builder()
 *         .email("john@example.com")
 *         .fullName("John Doe")
 *         .role(Role.GUEST)
 *         .build();
 *
 * // Checking permissions in service layer
 * if (user.getRole() == Role.HOST) {
 *     // Allow accommodation creation
 *     createAccommodation();
 * }
 *
 * // Using with Spring Security
 * @PreAuthorize("hasRole('HOST')")
 * public Accommodation createAccommodation(AccommodationDTO dto) {
 *     // Only hosts can access this method
 * }
 * }</pre>
 *
 * <p><b>Database Storage:</b></p>
 * The role is stored as a string ({@code EnumType.STRING}) in the database,
 * ensuring that the ordinal position does not affect data integrity if the
 * enum order changes in future versions.</p>
 *
 * <p><b>Future Considerations:</b></p>
 * <ul>
 *   <li>Adding an {@code ADMIN} role for platform management</li>
 *   <li>Supporting multiple roles per user (e.g., user can be both GUEST and HOST)</li>
 *   <li>Role-specific profile fields and verification requirements</li>
 * </ul>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see User
 */
public enum Role {

    /**
     * Guest role - A user who can search and book accommodations.
     *
     * <p>Guests are the travelers and customers of the platform. They can:
     * <ul>
     *   <li>Search for accommodations by location, dates, and filters</li>
     *   <li>View accommodation details, photos, and reviews</li>
     *   <li>Create and manage reservations</li>
     *   <li>Communicate with hosts</li>
     *   <li>Leave reviews after completed stays</li>
     *   <li>Manage their profile and payment methods</li>
     * </ul>
     *
     * <p><b>Restrictions:</b> Guests cannot list or manage accommodations.
     * To become a host, a guest would need to register a separate account
     * or upgrade their existing account (future feature).</p>
     *
     * <p><b>Default Role:</b> This is typically the default role assigned
     * during user registration.</p>
     */
    GUEST,

    /**
     * Host role - A user who can list and manage accommodations.
     *
     * <p>Hosts are the property owners and managers on the platform. They can:
     * <ul>
     *   <li>Create and publish accommodation listings</li>
     *   <li>Manage property details, photos, and pricing</li>
     *   <li>Set availability calendars and booking rules</li>
     *   <li>View and manage incoming reservations</li>
     *   <li>Communicate with guests</li>
     *   <li>Leave reviews for guests after completed stays</li>
     *   <li>Access the host dashboard with analytics and earnings</li>
     * </ul>
     *
     * <p><b>Restrictions:</b> Hosts cannot book their own accommodations.
     * They can, however, book other hosts' accommodations as guests (subject
     * to platform policies).</p>
     *
     * <p><b>Verification:</b> Hosts may be subject to additional verification
     * requirements, such as identity verification and property ownership proof.</p>
     *
     * <p><b>Responsibilities:</b> Hosts are responsible for:
     * <ul>
     *   <li>Maintaining accurate listing information</li>
     *   <li>Honoring confirmed reservations</li>
     *   <li>Providing safe and clean accommodation</li>
     *   <li>Complying with local laws and regulations</li>
     * </ul>
     */
    HOST
}

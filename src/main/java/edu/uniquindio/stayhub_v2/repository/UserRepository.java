package edu.uniquindio.stayhub_v2.repository;

import edu.uniquindio.stayhub_v2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 *
 * <p>This interface provides CRUD operations and custom query methods for
 * accessing and manipulating user data in the database. It extends Spring
 * Data JPA's {@link JpaRepository}, which provides standard database
 * operations out of the box.</p>
 *
 * <p><b>Inherited Methods (from JpaRepository):</b></p>
 * <ul>
 *   <li>{@code save(User)} - Persist or update a user</li>
 *   <li>{@code findById(Long)} - Find user by ID (including soft-deleted)</li>
 *   <li>{@code findAll()} - Retrieve all users (including soft-deleted)</li>
 *   <li>{@code delete(User)} - Hard delete (use with caution)</li>
 *   <li>{@code existsById(Long)} - Check if user exists by ID</li>
 *   <li>{@code count()} - Count total users</li>
 * </ul>
 *
 * <p><b>⚠️ Soft-Delete Consideration:</b></p>
 * Users support soft deletion via the {@code deleted} flag. Most queries should
 * exclude soft-deleted users. Consider adding methods like:
 * <ul>
 *   <li>{@code findByEmailAndDeletedFalse(String email)}</li>
 *   <li>{@code existsByEmailAndDeletedFalse(String email)}</li>
 * </ul>
 *
 * <p><b>Security Note:</b></p>
 * The email-based lookup is the primary method for authentication. Always
 * consider whether soft-deleted users should be able to authenticate or
 * reset their passwords.
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * @Service
 * public class UserService {
 *
 *     private final UserRepository userRepository;
 *
 *     public User loadUserByEmail(String email) {
 *         return userRepository.findByEmail(email)
 *                 .orElseThrow(() -> new UserNotFoundException(
 *                     "User not found with email: " + email));
 *     }
 *
 *     public boolean emailExists(String email) {
 *         return userRepository.findByEmail(email).isPresent();
 *     }
 *
 *     public User saveUser(User user) {
 *         return userRepository.save(user);
 *     }
 * }
 * }</pre>
 *
 * <p><b>Query Derivation:</b></p>
 * Spring Data JPA automatically implements query methods based on method names.
 * For example, {@code findByEmail} translates to:
 * <pre>
 * SELECT u FROM User u WHERE u.email = :email
 * </pre>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see JpaRepository
 * @see User
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     *
     * <p>This is the primary lookup method for user authentication and
     * email-based operations. The email address serves as the unique
     * identifier for user accounts in the system.</p>
     *
     * <p><b>Query Logic:</b></p>
     * <pre>
     * SELECT u FROM User u WHERE u.email = :email
     * </pre>
     *
     * <p><b>Return Value:</b></p>
     * <ul>
     *   <li>{@code Optional<User>} - Empty if no user exists with the given email</li>
     *   <li>Populated {@code Optional} if a matching user is found</li>
     * </ul>
     *
     * <p><b>⚠️ Important:</b> This method returns <b>all</b> users including
     * soft-deleted ones. If you need to exclude deleted users, use a custom
     * method like {@code findByEmailAndDeletedFalse(String email)}.</p>
     *
     * <p><b>Usage Patterns:</b></p>
     *
     * <p><i>1. Authentication:</i></p>
     * <pre>{@code
     * public UserDetails loadUserByUsername(String email) {
     *     User user = userRepository.findByEmail(email)
     *             .orElseThrow(() -> new UsernameNotFoundException(
     *                 "User not found: " + email));
     *
     *     return new org.springframework.security.core.userdetails.User(
     *             user.getEmail(),
     *             user.getPassword(),
     *             mapRolesToAuthorities(user.getRoles())
     *     );
     * }
     * }</pre>
     *
     * <p><i>2. Registration Validation:</i></p>
     * <pre>{@code
     * public void validateNewUserEmail(String email) {
     *     userRepository.findByEmail(email).ifPresent(user -> {
     *         throw new EmailAlreadyExistsException(
     *             "Email already registered: " + email);
     *     });
     * }
     * }</pre>
     *
     * <p><i>3. Password Recovery:</i></p>
     * <pre>{@code
     * public void initiatePasswordRecovery(String email) {
     *     User user = userRepository.findByEmail(email)
     *             .orElseThrow(() -> new UserNotFoundException(
     *                 "No account found with this email"));
     *
     *     String recoveryCode = generateRecoveryCode();
     *     user.setPasswordRecoveryCode(recoveryCode);
     *     user.setPasswordRecoveryExpiration(LocalDateTime.now().plusMinutes(30));
     *     userRepository.save(user);
     *
     *     emailService.sendRecoveryEmail(email, recoveryCode);
     * }
     * }</pre>
     *
     * <p><b>Performance:</b> This query uses the unique index on the email
     * column ({@code idx_user_email}), making it extremely efficient even
     * with millions of users.</p>
     *
     * <p><b>Case Sensitivity:</b> The comparison is case-sensitive by default
     * in most databases. Consider normalizing email addresses to lowercase
     * before querying and storing to ensure consistent behavior.</p>
     *
     * <p><b>Security Consideration:</b> When this method is used in
     * authentication flows, avoid revealing whether the email exists or not
     * to prevent user enumeration attacks. Always return the same generic
     * message for both "user not found" and "invalid password" scenarios.</p>
     *
     * @param email The email address to search for (should be normalized to lowercase)
     * @return An {@code Optional} containing the found user if it exists,
     *         otherwise an empty {@code Optional}
     */
    Optional<User> findByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(String email);

    Optional<User> findByEmailAndDeletedFalse(String email);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email AND u.deleted = false")
    Optional<User> findByEmailAndDeletedFalseWithRoles(String email);

    /*
     * Additional query methods that could be added in the future:
     *
     * // Find active (non-deleted) user by email
     * Optional<User> findByEmailAndDeletedFalse(String email);
     *
     * // Check if email exists (excluding deleted users)
     * boolean existsByEmailAndDeletedFalse(String email);
     *
     * // Find all users by role
     * List<User> findByRolesContaining(Role role);
     *
     * // Find users created between dates
     * List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
     *
     * // Search users by name (partial match)
     * @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
     * List<User> searchByFullName(@Param("name") String name);
     *
     * // Find users with expired recovery codes
     * @Query("SELECT u FROM User u WHERE u.passwordRecoveryExpiration < :now")
     * List<User> findUsersWithExpiredRecoveryCodes(@Param("now") LocalDateTime now);
     *
     * // Count users by role
     * @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = :role")
     * long countByRole(@Param("role") Role role);
     *
     * // Find users who haven't logged in since a date (requires lastLogin field)
     * List<User> findByLastLoginBefore(LocalDateTime date);
     */
}
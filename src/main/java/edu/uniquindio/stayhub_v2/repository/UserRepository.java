package edu.uniquindio.stayhub_v2.repository;

import edu.uniquindio.stayhub_v2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for User entities, providing methods for data access and persistence.
 * @author Esteban Gómez León
 * @version 1.0
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds a User entity by their email address.
     *
     * @param email The email address to search for.
     * @return An Optional containing the found User, or an empty Optional if no user is found.
     */
    Optional<User> findByEmail(String email);
}
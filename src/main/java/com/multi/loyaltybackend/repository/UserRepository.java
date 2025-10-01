package com.multi.loyaltybackend.repository;

import com.multi.loyaltybackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for {@link User} entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     *
     * @param email The email to search for.
     * @return An {@link Optional} containing the user if found, otherwise empty.
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by their unique username.
     *
     * @param username The username to search for.
     * @return An {@link Optional} containing the user if found, otherwise empty.
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their password reset token.
     *
     * @param token The password reset token.
     * @return An {@link Optional} containing the user if found, otherwise empty.
     */
    Optional<User> findByPasswordResetToken(String token);

    /**
     * Checks if a user exists with the given email.
     *
     * @param email The email to check.
     * @return true if a user with the email exists, false otherwise.
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a user exists with the given username.
     *
     * @param username The username to check.
     * @return true if a user with the username exists, false otherwise.
     */
    boolean existsByUsername(String username);
}
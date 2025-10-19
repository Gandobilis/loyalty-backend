package com.multi.loyaltybackend.repository;

import com.multi.loyaltybackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPasswordResetToken(String token);

    boolean existsByEmail(String email);
}
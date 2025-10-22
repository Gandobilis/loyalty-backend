package com.multi.loyaltybackend.repository;

import com.multi.loyaltybackend.model.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {
    Optional<PasswordResetCode> findByCodeAndEmail(String code, String email);

    Optional<PasswordResetCode> findByEmailAndUsedFalseAndExpiryTimeAfter(String email, LocalDateTime currentTime);

    @Modifying
    @Query("DELETE FROM PasswordResetCode p WHERE p.expiryTime < ?1")
    void deleteExpiredCodes(LocalDateTime currentTime);

    void deleteByEmail(String email);
}

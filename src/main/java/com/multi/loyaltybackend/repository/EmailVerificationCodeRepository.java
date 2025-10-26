package com.multi.loyaltybackend.repository;

import com.multi.loyaltybackend.model.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {

    Optional<EmailVerificationCode> findByCodeAndEmail(String code, String email);

    Optional<EmailVerificationCode> findByEmailAndUsedFalseAndExpiryTimeAfter(String email, LocalDateTime currentTime);

    @Modifying
    @Query("DELETE FROM EmailVerificationCode e WHERE e.expiryTime < ?1")
    void deleteExpiredCodes(LocalDateTime currentTime);

    void deleteByEmail(String email);
}

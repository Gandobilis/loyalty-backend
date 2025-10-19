package com.multi.loyaltybackend.repository;

import com.multi.loyaltybackend.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
}

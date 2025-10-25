package com.multi.loyaltybackend.repository;

import com.multi.loyaltybackend.model.Registration;
import com.multi.loyaltybackend.model.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegistrationRepository extends JpaRepository<Registration, Long>, JpaSpecificationExecutor<Registration> {
    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    @Query("SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId AND r.status IN :statuses")
    long countByEventIdAndStatusIn(@Param("eventId") Long eventId, @Param("statuses") java.util.List<RegistrationStatus> statuses);
}

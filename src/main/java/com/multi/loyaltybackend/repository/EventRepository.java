package com.multi.loyaltybackend.repository;

import com.multi.loyaltybackend.model.Event;
import com.multi.loyaltybackend.model.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId AND r.status = :status")
    Integer countRegistrationsForEventWithStatus(@Param("eventId") Long eventId, @Param("status") RegistrationStatus status);
}
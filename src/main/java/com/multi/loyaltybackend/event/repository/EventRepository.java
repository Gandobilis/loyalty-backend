package com.multi.loyaltybackend.event.repository;

import com.multi.loyaltybackend.event.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByCategoryAndDateTimeBetween(String category, LocalDate startDate, LocalDate endDate);
}
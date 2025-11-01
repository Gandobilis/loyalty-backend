package com.multi.loyaltybackend.repository;

import com.multi.loyaltybackend.model.SupportMessageResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportMessageResponseRepository extends JpaRepository<SupportMessageResponse, Long> {

    List<SupportMessageResponse> findBySupportMessageIdOrderByCreatedAtAsc(Long supportMessageId);
}

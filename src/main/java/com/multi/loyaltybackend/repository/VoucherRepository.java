package com.multi.loyaltybackend.repository;

import com.multi.loyaltybackend.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    List<Voucher> findByCompanyId(Long companyId);

    List<Voucher> findByExpiryAfter(LocalDateTime date);
}
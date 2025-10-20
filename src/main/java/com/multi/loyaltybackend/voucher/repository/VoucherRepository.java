package com.multi.loyaltybackend.voucher.repository;

import com.multi.loyaltybackend.voucher.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
}
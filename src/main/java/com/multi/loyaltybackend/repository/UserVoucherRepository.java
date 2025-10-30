package com.multi.loyaltybackend.repository;

import com.multi.loyaltybackend.model.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
    boolean existsByUserIdAndVoucherId(Long userId, Long voucherId);
    Optional<UserVoucher> getUserVoucherByUserIdAndVoucherId(Long userId, Long voucherId);
}

package com.multi.loyaltybackend.repository;

import com.multi.loyaltybackend.model.UserVoucher;
import com.multi.loyaltybackend.model.VoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
    List<UserVoucher> findByUserId(Long userId);

    List<UserVoucher> findByUserIdAndStatus(Long userId, VoucherStatus status);

    List<UserVoucher> findByVoucherId(Long voucherId);
}

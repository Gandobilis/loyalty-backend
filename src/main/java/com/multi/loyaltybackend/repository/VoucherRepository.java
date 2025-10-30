package com.multi.loyaltybackend.repository;

import com.multi.loyaltybackend.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long>, JpaSpecificationExecutor<Voucher> {
    List<Voucher> findByCompanyId(Long companyId);
    List<Voucher> findByCompanyIdIn(Set<Long> companyIds);

    @Query("SELECT v FROM Voucher v WHERE NOT EXISTS " +
            "(SELECT 1 FROM UserVoucher uv WHERE uv.voucher.id = v.id AND uv.user.id = :userId)")
    List<Voucher> findAvailableVouchersForUser(@Param("userId") Long userId);
}
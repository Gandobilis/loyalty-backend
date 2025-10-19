package com.multi.loyaltybackend.voucher.service;

import com.multi.loyaltybackend.model.VoucherStatus;
import com.multi.loyaltybackend.voucher.repository.UserVoucherRepository;
import com.multi.loyaltybackend.voucher.model.UserVoucher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserVoucherService {
    private final UserVoucherRepository userVoucherRepository;

    public List<UserVoucher> getAllUserVouchers() {
        return userVoucherRepository.findAll();
    }

    public Optional<UserVoucher> getUserVoucherById(Long id) {
        return userVoucherRepository.findById(id);
    }

    public List<UserVoucher> getUserVouchersByUserId(Long userId) {
        return userVoucherRepository.findByUserId(userId);
    }

    public List<UserVoucher> getUserVouchersByUserIdAndStatus(Long userId, VoucherStatus status) {
        return userVoucherRepository.findByUserIdAndStatus(userId, status);
    }

    @Transactional
    public UserVoucher assignVoucherToUser(UserVoucher userVoucher) {
        return userVoucherRepository.save(userVoucher);
    }

    @Transactional
    public UserVoucher redeemVoucher(Long id) {
        UserVoucher userVoucher = userVoucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("UserVoucher not found"));

        if (userVoucher.getStatus() != VoucherStatus.ACTIVE) {
            throw new RuntimeException("Voucher is not active");
        }

        userVoucher.setStatus(VoucherStatus.REDEEMED);
        userVoucher.setRedeemedAt(LocalDateTime.now());

        return userVoucherRepository.save(userVoucher);
    }

    @Transactional
    public void deleteUserVoucher(Long id) {
        userVoucherRepository.deleteById(id);
    }
}

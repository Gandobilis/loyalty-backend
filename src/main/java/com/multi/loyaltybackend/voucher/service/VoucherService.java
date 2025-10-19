package com.multi.loyaltybackend.voucher.service;

import com.multi.loyaltybackend.voucher.repository.VoucherRepository;
import com.multi.loyaltybackend.voucher.model.Voucher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoucherService {
    private final VoucherRepository voucherRepository;

    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    public Optional<Voucher> getVoucherById(Long id) {
        return voucherRepository.findById(id);
    }

    public List<Voucher> getVouchersByCompany(Long companyId) {
        return voucherRepository.findByCompanyId(companyId);
    }

    public List<Voucher> getActiveVouchers() {
        return voucherRepository.findByExpiryAfter(LocalDateTime.now());
    }

    @Transactional
    public Voucher createVoucher(Voucher voucher) {
        return voucherRepository.save(voucher);
    }

    @Transactional
    public Voucher updateVoucher(Long id, Voucher voucherDetails) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        voucher.setTitle(voucherDetails.getTitle());
        voucher.setDescription(voucherDetails.getDescription());
        voucher.setPoints(voucherDetails.getPoints());
        voucher.setExpiry(voucherDetails.getExpiry());

        return voucherRepository.save(voucher);
    }

    @Transactional
    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }
}
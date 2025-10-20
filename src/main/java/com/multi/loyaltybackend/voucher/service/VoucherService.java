package com.multi.loyaltybackend.voucher.service;

import com.multi.loyaltybackend.company.model.Company;
import com.multi.loyaltybackend.company.repository.CompanyRepository;
import com.multi.loyaltybackend.exception.DuplicateRegistrationException;
import com.multi.loyaltybackend.exception.ResourceNotFoundException;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.model.VoucherStatus;
import com.multi.loyaltybackend.repository.UserRepository;
import com.multi.loyaltybackend.service.ImageStorageService;
import com.multi.loyaltybackend.voucher.dto.UserVoucherRequest;
import com.multi.loyaltybackend.voucher.dto.VoucherRequest;
import com.multi.loyaltybackend.voucher.model.UserVoucher;
import com.multi.loyaltybackend.voucher.repository.UserVoucherRepository;
import com.multi.loyaltybackend.voucher.repository.VoucherRepository;
import com.multi.loyaltybackend.voucher.model.Voucher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoucherService {
    private final VoucherRepository voucherRepository;
    private final CompanyRepository companyRepository;
    private final ImageStorageService imageStorageService;
    private final UserRepository userRepository;
    private final UserVoucherRepository userVoucherRepository;

    public List<Voucher> getAllVouchers() {
        List<Voucher> vouchers = voucherRepository.findAll();
        vouchers.forEach(voucher -> {
            voucher.getCompany().setLogoFileName(imageStorageService.getFilePath(voucher.getCompany().getLogoFileName()));
        });
        return vouchers;
    }

    public Optional<Voucher> getVoucherById(Long id) {
        return voucherRepository.findById(id);
    }

    @Transactional
    public Voucher createVoucher(VoucherRequest request) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + request.getCompanyId()));

        Voucher voucher = new Voucher();
        voucher.setTitle(request.getTitle());
        voucher.setDescription(request.getDescription());
        voucher.setPoints(request.getPoints());
        voucher.setExpiry(request.getExpiry());
        voucher.setCompany(company);

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

    @Transactional
    public UserVoucher exchangeVoucher(Long userId, Long voucherId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", "id", voucherId));

        if (userVoucherRepository.existsByUserIdAndVoucherId(userId, voucherId)) {
            throw new RuntimeException(
                    String.format("User %d is already exchanged voucher %d", userId, voucherId)
            );
        } else if (user.getTotalPoints() < voucher.getPoints()) {
            throw new RuntimeException("Total points not enough");
        }

        UserVoucher userVoucher = UserVoucher.builder()
                .user(user)
                .voucher(voucher)
                .status(VoucherStatus.ACTIVE)
                .build();
        UserVoucher savedUserVoucher = userVoucherRepository.save(userVoucher);
        user.setTotalPoints(user.getTotalPoints() - voucher.getPoints());
        userRepository.save(user);

        return savedUserVoucher;
    }
}
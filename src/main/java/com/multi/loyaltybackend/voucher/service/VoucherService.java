package com.multi.loyaltybackend.voucher.service;

import com.multi.loyaltybackend.company.model.Company;
import com.multi.loyaltybackend.company.repository.CompanyRepository;
import com.multi.loyaltybackend.exception.*;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.model.VoucherStatus;
import com.multi.loyaltybackend.repository.UserRepository;
import com.multi.loyaltybackend.service.ImageStorageService;
import com.multi.loyaltybackend.voucher.dto.UserVoucherRequest;
import com.multi.loyaltybackend.voucher.dto.VoucherFilterDTO;
import com.multi.loyaltybackend.voucher.dto.VoucherRequest;
import com.multi.loyaltybackend.voucher.dto.VoucherWithCompanyDTO;
import com.multi.loyaltybackend.voucher.model.UserVoucher;
import com.multi.loyaltybackend.voucher.repository.UserVoucherRepository;
import com.multi.loyaltybackend.voucher.repository.VoucherRepository;
import com.multi.loyaltybackend.voucher.model.Voucher;
import com.multi.loyaltybackend.voucher.specification.VoucherSpecifications;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherService {
    private final VoucherRepository voucherRepository;
    private final CompanyRepository companyRepository;
    private final ImageStorageService imageStorageService;
    private final UserRepository userRepository;
    private final UserVoucherRepository userVoucherRepository;

    public Page<Voucher> getAllVouchers(Pageable pageable) {
        Page<Voucher> vouchers = voucherRepository.findAll(pageable);
        vouchers.forEach(voucher -> {
            if (voucher.getCompany() != null && voucher.getCompany().getLogoFileName() != null) {
                voucher.getCompany().setLogoFileName(imageStorageService.getFilePath(voucher.getCompany().getLogoFileName()));
            }
        });
        return vouchers;
    }

    public List<VoucherWithCompanyDTO> getAllVouchers() {
        List<Voucher> vouchers = voucherRepository.findAll();

        return vouchers.stream()
                .map(voucher -> {
                    VoucherWithCompanyDTO.VoucherWithCompanyDTOBuilder builder = VoucherWithCompanyDTO.builder()
                            .id(voucher.getId())
                            .title(voucher.getTitle())
                            .points(voucher.getPoints())
                            .expiry(voucher.getExpiry());

                    // Map company fields individually
                    if (voucher.getCompany() != null) {
                        Company company = voucher.getCompany();
                        builder.companyId(company.getId())
                                .companyName(company.getName())
                                .companyLogo(company.getLogoFileName() != null
                                        ? imageStorageService.getFilePath(company.getLogoFileName())
                                        : null);
                    }

                    return builder.build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get filtered vouchers with pagination
     */
    public Page<VoucherWithCompanyDTO> getFilteredVouchers(VoucherFilterDTO filter, Pageable pageable) {
        Specification<Voucher> spec = Specification.where(null);

        if (filter != null) {
            if (filter.getTitle() != null && !filter.getTitle().isEmpty()) {
                spec = spec.and(VoucherSpecifications.titleContains(filter.getTitle()));
            }
            if (filter.getCompanyId() != null) {
                spec = spec.and(VoucherSpecifications.hasCompanyId(filter.getCompanyId()));
            }
            if (filter.getMinPoints() != null) {
                spec = spec.and(VoucherSpecifications.hasPointsGreaterThanOrEqual(filter.getMinPoints()));
            }
            if (filter.getMaxPoints() != null) {
                spec = spec.and(VoucherSpecifications.hasPointsLessThanOrEqual(filter.getMaxPoints()));
            }
            if (filter.getExpiryFrom() != null) {
                spec = spec.and(VoucherSpecifications.expiresAfter(filter.getExpiryFrom().atStartOfDay()));
            }
            if (filter.getExpiryTo() != null) {
                spec = spec.and(VoucherSpecifications.expiresBefore(filter.getExpiryTo().atTime(23, 59, 59)));
            }
            if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                spec = spec.and(VoucherSpecifications.hasStatus(filter.getStatus()));
            }
        }

        Page<Voucher> vouchers = voucherRepository.findAll(spec, pageable);
        return vouchers.map(voucher -> {
            VoucherWithCompanyDTO.VoucherWithCompanyDTOBuilder builder = VoucherWithCompanyDTO.builder()
                    .id(voucher.getId())
                    .title(voucher.getTitle())
                    .points(voucher.getPoints())
                    .expiry(voucher.getExpiry());

            // Map company fields individually
            if (voucher.getCompany() != null) {
                Company company = voucher.getCompany();
                builder.companyId(company.getId())
                        .companyName(company.getName())
                        .companyLogo(company.getLogoFileName() != null
                                ? imageStorageService.getFilePath(company.getLogoFileName())
                                : null);
            }

            return builder.build();
        });
    }

    public Optional<VoucherWithCompanyDTO> getVoucherById(Long id) {
        return voucherRepository.findById(id)
                .map(voucher -> {
                    VoucherWithCompanyDTO.VoucherWithCompanyDTOBuilder builder = VoucherWithCompanyDTO.builder()
                            .id(voucher.getId())
                            .title(voucher.getTitle())
                            .points(voucher.getPoints())
                            .expiry(voucher.getExpiry());

                    // Map company fields individually
                    if (voucher.getCompany() != null) {
                        Company company = voucher.getCompany();
                        builder.companyId(company.getId())
                                .companyName(company.getName())
                                .companyLogo(company.getLogoFileName() != null
                                        ? imageStorageService.getFilePath(company.getLogoFileName())
                                        : null);
                    }

                    return builder.build();
                });
    }

    @Transactional
    public Voucher createVoucher(VoucherRequest request) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException(request.getCompanyId()));

        Voucher voucher = new Voucher();
        voucher.setTitle(request.getTitle());
        voucher.setPoints(request.getPoints());
        voucher.setExpiry(request.getExpiry());
        voucher.setCompany(company);

        return voucherRepository.save(voucher);
    }

    @Transactional
    public Voucher updateVoucher(Long id, Voucher voucherDetails) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new VoucherNotFoundException(id));

        voucher.setTitle(voucherDetails.getTitle());
        voucher.setPoints(voucherDetails.getPoints());
        voucher.setExpiry(voucherDetails.getExpiry());
        voucher.setCompany(voucherDetails.getCompany());

        return voucherRepository.save(voucher);
    }

    @Transactional
    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }

    @Transactional
    public UserVoucher exchangeVoucher(Long userId, Long voucherId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new VoucherNotFoundException(voucherId));

        // Check if voucher has expired
        if (voucher.getExpiry() != null && voucher.getExpiry().isBefore(LocalDateTime.now())) {
            throw new VoucherExpiredException(voucherId);
        }

        // Check if user has already exchanged this voucher
        if (userVoucherRepository.existsByUserIdAndVoucherId(userId, voucherId)) {
            throw new VoucherAlreadyExchangedException(userId, voucherId);
        }

        // Check if user has sufficient points
        if (user.getTotalPoints() < voucher.getPoints()) {
            throw new InsufficientPointsException(voucher.getPoints(), user.getTotalPoints());
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
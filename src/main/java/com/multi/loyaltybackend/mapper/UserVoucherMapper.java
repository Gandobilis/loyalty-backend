package com.multi.loyaltybackend.mapper;

import com.multi.loyaltybackend.model.Company;
import com.multi.loyaltybackend.dto.response.UserVoucherResponse;
import com.multi.loyaltybackend.service.ImageStorageService;
import com.multi.loyaltybackend.model.UserVoucher;
import com.multi.loyaltybackend.model.Voucher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserVoucherMapper {
    private final ImageStorageService imageStorageService;

    public UserVoucherResponse toResponse(UserVoucher userVoucher) {
        Voucher voucher = userVoucher.getVoucher();
        Company company = voucher.getCompany();

        return UserVoucherResponse.builder()
                .id(voucher.getId())
                .title(voucher.getTitle())
                .points(voucher.getPoints())
                .expiry(voucher.getExpiry())
                .status(userVoucher.getStatus())
                .companyName(company.getName())
                .companyLogoFileName(imageStorageService.getFilePath(company.getLogoFileName()))
                .build();
    }

    public List<UserVoucherResponse> toResponseList(List<UserVoucher> userVouchers) {
        return userVouchers.stream()
                .map(this::toResponse)
                .toList();
    }
}
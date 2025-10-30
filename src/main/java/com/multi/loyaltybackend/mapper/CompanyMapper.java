package com.multi.loyaltybackend.mapper;


import com.multi.loyaltybackend.dto.CompanyResponseDTO;
import com.multi.loyaltybackend.dto.VoucherDTO;
import com.multi.loyaltybackend.model.Company;
import com.multi.loyaltybackend.model.Voucher;

import java.util.List;
import java.util.stream.Collectors;

public class CompanyMapper {

    public static CompanyResponseDTO toResponseDTO(Company company, List<Voucher> vouchers) {
        return CompanyResponseDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .logoFileName(company.getLogoFileName())
                .vouchers(vouchers.stream()
                        .map(CompanyMapper::toVoucherDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    public static VoucherDTO toVoucherDTO(Voucher voucher) {
        return VoucherDTO.builder()
                .id(voucher.getId())
                .title(voucher.getTitle())
                .points(voucher.getPoints())
                .expiry(voucher.getExpiry())
                .build();
    }
}
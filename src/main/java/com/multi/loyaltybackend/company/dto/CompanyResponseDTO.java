package com.multi.loyaltybackend.company.dto;

import com.multi.loyaltybackend.voucher.dto.VoucherDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponseDTO {
    private Long id;
    private String name;
    private String logoFileName;
    private List<VoucherDTO> vouchers;
}
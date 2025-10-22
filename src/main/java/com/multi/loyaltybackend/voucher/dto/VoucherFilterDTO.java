package com.multi.loyaltybackend.voucher.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherFilterDTO {
    private String title;
    private Long companyId;
    private Integer minPoints;
    private Integer maxPoints;
    private LocalDate expiryFrom;
    private LocalDate expiryTo;
    private String status; // "active" or "expired"
}

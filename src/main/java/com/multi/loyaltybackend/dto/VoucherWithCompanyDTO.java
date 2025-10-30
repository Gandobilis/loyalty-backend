package com.multi.loyaltybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherWithCompanyDTO {
    private Long id;
    private String title;
    private String description;
    private Integer points;
    private LocalDateTime expiry;
    private Long companyId;
    private String companyName;
    private String companyLogo;
}
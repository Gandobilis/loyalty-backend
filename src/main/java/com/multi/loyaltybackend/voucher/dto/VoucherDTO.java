package com.multi.loyaltybackend.voucher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherDTO {
    private Long id;
    private String title;
    private String description;
    private Integer points;
    private LocalDateTime expiry;
}

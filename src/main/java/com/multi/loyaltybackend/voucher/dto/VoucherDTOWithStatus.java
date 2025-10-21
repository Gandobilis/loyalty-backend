package com.multi.loyaltybackend.voucher.dto;

import com.multi.loyaltybackend.model.VoucherStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDTOWithStatus {
    private Long id;
    private String title;
    private String description;
    private Integer points;
    private LocalDateTime expiry;
    private VoucherStatus status;
}
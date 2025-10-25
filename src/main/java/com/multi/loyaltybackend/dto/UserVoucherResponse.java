package com.multi.loyaltybackend.dto;

import com.multi.loyaltybackend.model.VoucherStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserVoucherResponse {
    private Long id;
    private String title;
    private Integer points;
    private LocalDateTime expiry;
    private VoucherStatus status;
    private String companyName;
    private String companyLogoFileName;
}

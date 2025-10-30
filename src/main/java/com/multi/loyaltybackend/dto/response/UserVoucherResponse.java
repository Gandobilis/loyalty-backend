package com.multi.loyaltybackend.dto.response;

import com.multi.loyaltybackend.model.VoucherStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
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

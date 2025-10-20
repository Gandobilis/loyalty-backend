package com.multi.loyaltybackend.voucher.dto;

import com.multi.loyaltybackend.model.VoucherStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVoucherRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Voucher ID is required")
    private Long voucherId;
}
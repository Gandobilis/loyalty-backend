package com.multi.loyaltybackend.voucher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Points are required")
    @Positive(message = "Points must be positive")
    private Integer points;

    @NotNull(message = "Expiry date is required")
    private LocalDateTime expiry;

    @NotNull(message = "Company ID is required")
    @Positive(message = "Company ID must be positive")
    private Long companyId;
}
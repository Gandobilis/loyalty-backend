package com.multi.loyaltybackend.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserVoucherRequest(
        @NotNull(message = "ვაუჩერის ID აუცილებელია")
        Long voucherId
) {
}
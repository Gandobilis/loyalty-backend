package com.multi.loyaltybackend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to use an expired voucher.
 */
@Getter
public class VoucherExpiredException extends BaseException {

    private final Long voucherId;

    public VoucherExpiredException(Long voucherId) {
        super(
                ErrorCode.VOUCHER_EXPIRED,
                HttpStatus.BAD_REQUEST,
                String.format("Voucher %d has expired", voucherId)
        );
        this.voucherId = voucherId;
        addContext("voucherId", voucherId);
    }

    public VoucherExpiredException(String message) {
        super(ErrorCode.VOUCHER_EXPIRED, HttpStatus.BAD_REQUEST, message);
        this.voucherId = null;
    }
}

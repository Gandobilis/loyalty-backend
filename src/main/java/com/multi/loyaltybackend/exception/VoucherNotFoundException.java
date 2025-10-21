package com.multi.loyaltybackend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested voucher is not found.
 */
@Getter
public class VoucherNotFoundException extends BaseException {

    private final Long voucherId;

    public VoucherNotFoundException(Long id) {
        super(
                ErrorCode.VOUCHER_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                String.format("Voucher not found with id: %d", id)
        );
        this.voucherId = id;
        addContext("voucherId", id);
    }

    public VoucherNotFoundException(String message) {
        super(ErrorCode.VOUCHER_NOT_FOUND, HttpStatus.NOT_FOUND, message);
        this.voucherId = null;
    }
}

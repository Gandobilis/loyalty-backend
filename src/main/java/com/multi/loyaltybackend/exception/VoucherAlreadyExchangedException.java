package com.multi.loyaltybackend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user attempts to exchange a voucher they have already exchanged.
 */
@Getter
public class VoucherAlreadyExchangedException extends BaseException {

    private final Long userId;
    private final Long voucherId;

    public VoucherAlreadyExchangedException(Long userId, Long voucherId) {
        super(
                ErrorCode.VOUCHER_ALREADY_EXCHANGED,
                HttpStatus.CONFLICT,
                String.format("User %d has already exchanged voucher %d", userId, voucherId)
        );
        this.userId = userId;
        this.voucherId = voucherId;

        // Add context for debugging
        addContext("userId", userId);
        addContext("voucherId", voucherId);
    }

    public VoucherAlreadyExchangedException(String message) {
        super(ErrorCode.VOUCHER_ALREADY_EXCHANGED, HttpStatus.CONFLICT, message);
        this.userId = null;
        this.voucherId = null;
    }
}

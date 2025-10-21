package com.multi.loyaltybackend.exception;

public class VoucherAlreadyExchangedException extends RuntimeException {
    public VoucherAlreadyExchangedException(Long userId, Long voucherId) {
        super("User " + userId + " has already exchanged voucher " + voucherId);
    }

    public VoucherAlreadyExchangedException(String message) {
        super(message);
    }
}

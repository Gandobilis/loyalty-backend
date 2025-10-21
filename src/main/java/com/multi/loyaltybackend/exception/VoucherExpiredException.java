package com.multi.loyaltybackend.exception;

public class VoucherExpiredException extends RuntimeException {
    public VoucherExpiredException(Long voucherId) {
        super("Voucher " + voucherId + " has expired");
    }

    public VoucherExpiredException(String message) {
        super(message);
    }
}

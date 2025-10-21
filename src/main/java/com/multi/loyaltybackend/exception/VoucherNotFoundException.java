package com.multi.loyaltybackend.exception;

public class VoucherNotFoundException extends ResourceNotFoundException {
    public VoucherNotFoundException(Long id) {
        super("Voucher not found with id: " + id);
    }

    public VoucherNotFoundException(String message) {
        super(message);
    }
}

package com.multi.loyaltybackend.exception;

public class CompanyNotFoundException extends RuntimeException {
    public CompanyNotFoundException(Long id) {
        super("Company not found with id: " + id);
    }

    public CompanyNotFoundException(String message) {
        super(message);
    }
}

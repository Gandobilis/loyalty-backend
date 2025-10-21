package com.multi.loyaltybackend.exception;

public class CompanyNotFoundException extends ResourceNotFoundException {
    public CompanyNotFoundException(Long id) {
        super("Company not found with id: " + id);
    }

    public CompanyNotFoundException(String message) {
        super(message);
    }
}

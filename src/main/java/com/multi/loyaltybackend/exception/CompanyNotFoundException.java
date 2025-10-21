package com.multi.loyaltybackend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested company is not found.
 */
@Getter
public class CompanyNotFoundException extends BaseException {

    private final Long companyId;

    public CompanyNotFoundException(Long id) {
        super(
                ErrorCode.COMPANY_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                String.format("Company not found with id: %d", id)
        );
        this.companyId = id;
        addContext("companyId", id);
    }

    public CompanyNotFoundException(String message) {
        super(ErrorCode.COMPANY_NOT_FOUND, HttpStatus.NOT_FOUND, message);
        this.companyId = null;
    }
}

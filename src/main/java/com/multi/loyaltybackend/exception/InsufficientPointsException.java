package com.multi.loyaltybackend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user has insufficient points for an operation.
 * Includes details about required and available points.
 */
@Getter
public class InsufficientPointsException extends BaseException {

    private final int requiredPoints;
    private final int availablePoints;

    public InsufficientPointsException(int requiredPoints, int availablePoints) {
        super(
                ErrorCode.INSUFFICIENT_POINTS,
                HttpStatus.BAD_REQUEST,
                String.format("Insufficient points. Required: %d, Available: %d", requiredPoints, availablePoints)
        );
        this.requiredPoints = requiredPoints;
        this.availablePoints = availablePoints;

        // Add context for debugging
        addContext("requiredPoints", requiredPoints);
        addContext("availablePoints", availablePoints);
    }

    public InsufficientPointsException(String message) {
        super(ErrorCode.INSUFFICIENT_POINTS, HttpStatus.BAD_REQUEST, message);
        this.requiredPoints = 0;
        this.availablePoints = 0;
    }
}

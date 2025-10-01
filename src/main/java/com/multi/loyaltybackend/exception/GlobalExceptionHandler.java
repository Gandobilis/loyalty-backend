package com.multi.loyaltybackend.exception;

import com.multi.loyaltybackend.dto.ApiResponze;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Add a logger to record unexpected errors
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponze<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponze<Void> response = ApiResponze.error("Validation Failed", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponze<Void>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        ApiResponze<Void> response = ApiResponze.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Catches all other exceptions that are not explicitly handled.
     * This acts as a safety net to prevent exposing stack traces to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponze<Void>> handleGeneralException(Exception ex) {
        // Log the exception for debugging purposes
        logger.error("An unexpected error occurred: {}", ex.getMessage(), ex);

        // Return a generic error message to the client for security
        ApiResponze<Void> response = ApiResponze.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
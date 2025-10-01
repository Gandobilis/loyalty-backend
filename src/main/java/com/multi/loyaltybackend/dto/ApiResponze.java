package com.multi.loyaltybackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // Don't include null fields in the JSON
public class ApiResponze<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final Object errors;

    // Private constructor
    private ApiResponze(boolean success, String message, T data, Object errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    // Static factory for success responses
    public static <T> ApiResponze<T> success(T data, String message) {
        return new ApiResponze<>(true, message, data, null);
    }

    // Static factory for simple success responses without data
    public static <T> ApiResponze<T> success(String message) {
        return new ApiResponze<>(true, message, null, null);
    }

    // Static factory for error responses
    public static <T> ApiResponze<T> error(String message, Object errors) {
        return new ApiResponze<>(false, message, null, errors);
    }

    // Static factory for simple error responses
    public static <T> ApiResponze<T> error(String message) {
        return new ApiResponze<>(false, message, null, null);
    }
}
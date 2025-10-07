package com.multi.loyaltybackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final int status;
    private final boolean success;
    private final String message;
    private final T data;
    private final Object errors;

    private ApiResponse(HttpStatus status, boolean success, String message, T data, Object errors) {
        this.status = status.value();
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    public static <T> ApiResponse<T> success(HttpStatus status, T data, String message) {
        return new ApiResponse<>(status, true, message, data, null);
    }

    public static <T> ApiResponse<T> success(HttpStatus status, String message) {
        return new ApiResponse<>(status, true, message, null, null);
    }

    public static <T> ApiResponse<T> error(HttpStatus status, String message, Object errors) {
        return new ApiResponse<>(status, false, message, null, errors);
    }

    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return new ApiResponse<>(status, false, message, null, null);
    }
}
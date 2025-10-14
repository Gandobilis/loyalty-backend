package com.multi.loyaltybackend.event.dto;

import com.multi.loyaltybackend.event.enums.EventCategory;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record EventRequestDTO(
        @NotBlank(message = "Title is required") @Size(max = 100, message = "Title cannot exceed 100 characters") String title,
        @Size(max = 255, message = "Short description cannot exceed 255 characters") String shortDescription,
        String description, @NotNull(message = "Category is required") EventCategory category,
        @Size(max = 255, message = "Address cannot exceed 255 characters") String address,
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90") @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90") Double latitude,
        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180") @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180") Double longitude,
        @NotNull(message = "Date and time is required") @Future(message = "Event date must be in the future") LocalDateTime dateTime,
        @NotNull(message = "Points value is required") @Min(value = 0, message = "Points must be zero or positive") Integer points) {
}
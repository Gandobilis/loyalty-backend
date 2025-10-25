package com.multi.loyaltybackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record RegistrationRequest(@NotNull(message = "Event ID is required") Long eventId,
                                  @Size(max = 500, message = "Comment cannot exceed 500 characters") String comment) {
}
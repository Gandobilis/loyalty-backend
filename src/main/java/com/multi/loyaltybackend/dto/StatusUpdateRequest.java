package com.multi.loyaltybackend.dto;

import com.multi.loyaltybackend.model.RegistrationStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull(message = "Status is required") RegistrationStatus status) {
}
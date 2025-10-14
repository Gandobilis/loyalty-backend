package com.multi.loyaltybackend.registration.dto;

import com.multi.loyaltybackend.registration.enums.RegistrationStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull(message = "Status is required") RegistrationStatus status) {
}
package com.multi.loyaltybackend.registration.dto;

import com.multi.loyaltybackend.registration.enums.RegistrationStatus;

import java.time.LocalDateTime;

public record RegistrationResponse(Long id, Long userId, String userName, Long eventId, String eventTitle,
                                   String comment, RegistrationStatus status, LocalDateTime registeredAt,
                                   LocalDateTime updatedAt) {
}
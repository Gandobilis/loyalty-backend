package com.multi.loyaltybackend.dto;

import com.multi.loyaltybackend.model.RegistrationStatus;

import java.time.LocalDateTime;

public record RegistrationResponse(Long id, Long userId, String userName, Long eventId, String eventTitle,
                                   String comment, RegistrationStatus status, LocalDateTime registeredAt,
                                   LocalDateTime updatedAt) {
}
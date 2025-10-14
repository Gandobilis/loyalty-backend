package com.multi.loyaltybackend.event.dto;

import com.multi.loyaltybackend.event.enums.EventCategory;

import java.time.LocalDateTime;
import java.util.List;

public record EventResponseDTO(Long id, String fileName, String title, String shortDescription, String description,
                               EventCategory category, String address, Double latitude, Double longitude,
                               LocalDateTime dateTime, LocalDateTime createdAt, LocalDateTime updatedAt,
                               List<String> registrations) {
}
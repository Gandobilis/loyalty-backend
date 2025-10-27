package com.multi.loyaltybackend.dto;

import com.multi.loyaltybackend.model.EventCategory;

import java.time.LocalDateTime;
import java.util.List;

public record EventResponseDTO(Long id, String fileName, String title, String shortDescription, String description,
                               EventCategory category, String address, Double latitude, Double longitude,
                               LocalDateTime dateTime,Integer participantsPending, Integer participantsRegistered, Integer participantsCompleted, Integer participantsCancelled, Integer maxParticipants, LocalDateTime createdAt, LocalDateTime updatedAt,
                               List<UserDTO> registrations) {
}
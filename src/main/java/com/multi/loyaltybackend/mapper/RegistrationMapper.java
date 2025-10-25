package com.multi.loyaltybackend.mapper;

import com.multi.loyaltybackend.dto.RegistrationManagementDTO;
import com.multi.loyaltybackend.model.Registration;
import org.springframework.stereotype.Component;

@Component
public class RegistrationMapper {

    public RegistrationManagementDTO toManagementDTO(Registration registration) {
        if (registration == null) {
            return null;
        }

        return RegistrationManagementDTO.builder()
                .id(registration.getId())
                .userId(registration.getUser().getId())
                .userEmail(registration.getUser().getEmail())
                .userFullName(registration.getUser().getFullName())
                .eventId(registration.getEvent().getId())
                .eventTitle(registration.getEvent().getTitle())
                .eventDescription(registration.getEvent().getDescription())
                .eventCategory(registration.getEvent().getCategory().name())
                .eventDateTime(registration.getEvent().getDateTime())
                .eventPoints(registration.getEvent().getPoints())
                .eventVolunteerLimit(registration.getEvent().getVolunteer())
                .comment(registration.getComment())
                .status(registration.getStatus())
                .registeredAt(registration.getRegisteredAt())
                .updatedAt(registration.getUpdatedAt())
                .build();
    }
}

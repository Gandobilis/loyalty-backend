package com.multi.loyaltybackend.mapper;

import com.multi.loyaltybackend.dto.response.ProfileResponse;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfileMapper {
    private final ImageStorageService imageStorageService;

    public ProfileResponse toResponse(User user) {
        return ProfileResponse.builder()
                .fullName(user.getFullName())
                .age(user.getAge())
                .mobileNumber(user.getMobileNumber())
                .aboutMe(user.getAboutMe())
                .fileName(imageStorageService.getFilePath(user.getFileName()))
                .totalPoints(user.getTotalPoints())
                .eventCount(user.getEventCount())
                .workingHours(user.getWorkingHours())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

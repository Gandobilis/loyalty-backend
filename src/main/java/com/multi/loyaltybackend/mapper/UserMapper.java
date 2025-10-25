package com.multi.loyaltybackend.mapper;

import com.multi.loyaltybackend.dto.UserFormDTO;
import com.multi.loyaltybackend.dto.UserManagementDTO;
import com.multi.loyaltybackend.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserManagementDTO toManagementDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserManagementDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .totalPoints(user.getTotalPoints())
                .eventCount(user.getEventCount())
                .workingHours(user.getWorkingHours())
                .mobileNumber(user.getMobileNumber())
                .age(user.getAge())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .aboutMe(user.getAboutMe())
                .build();
    }

    public User toEntity(UserFormDTO dto) {
        if (dto == null) {
            return null;
        }

        return User.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .age(dto.getAge())
                .mobileNumber(dto.getMobileNumber())
                .role(dto.getRole())
                .totalPoints(dto.getTotalPoints())
                .eventCount(dto.getEventCount())
                .workingHours(dto.getWorkingHours())
                .aboutMe(dto.getAboutMe())
                .build();
    }

    public void updateEntityFromDTO(User user, UserFormDTO dto) {
        if (user == null || dto == null) {
            return;
        }

        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setAge(dto.getAge());
        user.setMobileNumber(dto.getMobileNumber());
        user.setRole(dto.getRole());
        user.setTotalPoints(dto.getTotalPoints());
        user.setEventCount(dto.getEventCount());
        user.setWorkingHours(dto.getWorkingHours());
        user.setAboutMe(dto.getAboutMe());
    }
}

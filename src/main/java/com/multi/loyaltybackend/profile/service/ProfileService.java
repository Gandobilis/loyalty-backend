package com.multi.loyaltybackend.profile.service;

import com.multi.loyaltybackend.file.ImageStorageService;
import com.multi.loyaltybackend.profile.dto.ProfileResponseDTO;
import com.multi.loyaltybackend.auth.model.User;
import com.multi.loyaltybackend.auth.repository.UserRepository;
import com.multi.loyaltybackend.profile.dto.ProfileUpdateDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final ImageStorageService fileStorageService;

    public ProfileResponseDTO getProfile(String email) {
        User user = findUserByEmail(email);
        return mapToProfileResponseDTO(user);
    }

    public ProfileResponseDTO updateProfile(String email, ProfileUpdateDTO dto) {
        User user = findUserByEmail(email);

        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getAge() != null) user.setAge(dto.getAge());
        if (dto.getMobileNumber() != null) user.setMobileNumber(dto.getMobileNumber());
        if (dto.getAboutMe() != null) user.setAboutMe(dto.getAboutMe());

        return mapToProfileResponseDTO(userRepository.save(user));
    }

    @Transactional
    public ProfileResponseDTO uploadProfileImage(String email, MultipartFile image) {
        User user = findUserByEmail(email);

        String newFileName = fileStorageService.storeFile(image);
        String oldFileName = user.getFileName();

        user.setFileName(newFileName);
        ProfileResponseDTO response = mapToProfileResponseDTO(userRepository.save(user));

        deleteOldFileIfNeeded(oldFileName, newFileName);
        return response;
    }

    @Transactional
    public void deleteProfileImage(String email) {
        User user = findUserByEmail(email);
        String fileName = user.getFileName();

        if (fileName != null && !fileName.isEmpty()) {
            try {
                fileStorageService.deleteFile(fileName);
            } catch (RuntimeException e) {
                System.err.println("Warning: Failed to delete file: " + fileName);
            }
            user.setFileName(null);
            userRepository.save(user);
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    private void deleteOldFileIfNeeded(String oldFile, String newFile) {
        if (oldFile != null && !oldFile.equals(newFile)) {
            try {
                fileStorageService.deleteFile(oldFile);
            } catch (Exception e) {
                System.err.println("Failed to delete old file: " + oldFile);
            }
        }
    }

    private ProfileResponseDTO mapToProfileResponseDTO(User user) {
        return ProfileResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .age(user.getAge())
                .mobileNumber(user.getMobileNumber())
                .aboutMe(user.getAboutMe())
                .fileName(user.getFileName() != null ? fileStorageService.getFilePath(user.getFileName()) : null)
                .role(user.getRole())
                .totalPoints(user.getTotalPoints())
                .eventCount(user.getEventCount())
                .workingHours(user.getWorkingHours())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

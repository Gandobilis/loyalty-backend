package com.multi.loyaltybackend.service;

import com.multi.loyaltybackend.dto.EventDTO;
import com.multi.loyaltybackend.dto.ProfileResponseDTO;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.repository.UserRepository;
import com.multi.loyaltybackend.dto.ProfileUpdateDTO;
import com.multi.loyaltybackend.voucher.dto.VoucherDTO;
import com.multi.loyaltybackend.voucher.dto.VoucherDTOWithStatus;
import com.multi.loyaltybackend.voucher.model.Voucher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

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
            fileStorageService.deleteFile(fileName);
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
            fileStorageService.deleteFile(oldFile);
        }
    }

    private ProfileResponseDTO mapToProfileResponseDTO(User user) {
        return ProfileResponseDTO.builder()
                .registrations(user.getRegistrations().stream()
                        .map(registration -> new EventDTO(
                                registration.getEvent().getId(),
                                registration.getEvent().getTitle(),
                                registration.getEvent().getPoints(),
                                registration.getEvent().getDateTime(),
                                (registration.getEvent().getFileName() != null ? fileStorageService.getFilePath(registration.getEvent().getFileName()) : null),
                                registration.getStatus()
                        ))
                        .collect(Collectors.toList()))
                .vouchers(user.getUserVouchers().stream().map(userVoucher -> {
                    Voucher voucher = userVoucher.getVoucher();
                    return VoucherDTOWithStatus.builder()
                            .id(voucher.getId())
                            .title(voucher.getTitle())
                            .points(voucher.getPoints())
                            .description(voucher.getDescription())
                            .expiry(voucher.getExpiry())
                            .status(userVoucher.getStatus())
                            .build();
                }).toList())
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
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

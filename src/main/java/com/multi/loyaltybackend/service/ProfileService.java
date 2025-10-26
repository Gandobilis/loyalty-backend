package com.multi.loyaltybackend.service;

import com.multi.loyaltybackend.company.model.Company;
import com.multi.loyaltybackend.dto.ChangePasswordRequest;
import com.multi.loyaltybackend.dto.ProfileResponse;
import com.multi.loyaltybackend.dto.UserEventResponse;
import com.multi.loyaltybackend.dto.UserVoucherResponse;
import com.multi.loyaltybackend.exception.InvalidCurrentPasswordException;
import com.multi.loyaltybackend.model.Event;
import com.multi.loyaltybackend.model.Registration;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.repository.UserRepository;
import com.multi.loyaltybackend.dto.ProfileUpdateRequest;
import com.multi.loyaltybackend.voucher.model.UserVoucher;
import com.multi.loyaltybackend.voucher.model.Voucher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final ImageStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;
    public ProfileResponse getProfile(String email) {
        User user = findUserByEmail(email);
        return mapToProfileResponseDTO(user);
    }

    public ProfileResponse updateProfile(String email, ProfileUpdateRequest dto) {
        User user = findUserByEmail(email);

        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getAge() != null) user.setAge(dto.getAge());
        if (dto.getMobileNumber() != null) user.setMobileNumber(dto.getMobileNumber());
        if (dto.getAboutMe() != null) user.setAboutMe(dto.getAboutMe());

        return mapToProfileResponseDTO(userRepository.save(user));
    }

    @Transactional
    public ProfileResponse uploadProfileImage(String email, MultipartFile image) {
        User user = findUserByEmail(email);

        String newFileName = fileStorageService.storeFile(image);
        String oldFileName = user.getFileName();

        user.setFileName(newFileName);
        ProfileResponse response = mapToProfileResponseDTO(userRepository.save(user));

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

    private ProfileResponse mapToProfileResponseDTO(User user) {
        return ProfileResponse.builder()
                .fullName(user.getFullName())
                .age(user.getAge())
                .mobileNumber(user.getMobileNumber())
                .aboutMe(user.getAboutMe())
                .fileName(user.getFileName() != null ? fileStorageService.getFilePath(user.getFileName()) : null)
                .totalPoints(user.getTotalPoints())
                .eventCount(user.getEventCount())
                .workingHours(user.getWorkingHours())
                .build();
    }

    public List<UserEventResponse> getUserEvents(String email) {
        User user = findUserByEmail(email);
        List<Registration> registrations = user.getRegistrations();
        return registrations.stream().map(registration -> {
            Event event = registration.getEvent();
            return UserEventResponse.builder()
                    .id(event.getId())
                    .title(event.getTitle())
                    .points(event.getPoints())
                    .dateTime(event.getDateTime())
                    .status(registration.getStatus())
                    .build();
        }).toList();
    }

    public List<UserVoucherResponse> getUserVouchers(String email) {
        User user = findUserByEmail(email);
        List<UserVoucher> vouchers = user.getUserVouchers();
        return vouchers.stream().map(userVoucher -> {
            Voucher voucher = userVoucher.getVoucher();
            Company company = voucher.getCompany();
            return UserVoucherResponse.builder()
                    .id(voucher.getId())
                    .title(voucher.getTitle())
                    .points(voucher.getPoints())
                    .expiry(voucher.getExpiry())
                    .status(userVoucher.getStatus())
                    .companyName(company.getName())
                    .companyLogoFileName(company.getLogoFileName() != null ? fileStorageService.getFilePath(company.getLogoFileName()) : null)
                    .build();
        }).toList();
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findUserByEmail(email);

        // Verify current password
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidCurrentPasswordException();
        }

        // Validate new password and confirm password match
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}

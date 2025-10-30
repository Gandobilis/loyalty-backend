package com.multi.loyaltybackend.service;

import com.multi.loyaltybackend.dto.request.ChangePasswordRequest;
import com.multi.loyaltybackend.dto.response.ProfileResponse;
import com.multi.loyaltybackend.dto.response.UserEventResponse;
import com.multi.loyaltybackend.dto.response.UserVoucherResponse;
import com.multi.loyaltybackend.exception.InvalidCurrentPasswordException;
import com.multi.loyaltybackend.mapper.ProfileMapper;
import com.multi.loyaltybackend.mapper.UserEventMapper;
import com.multi.loyaltybackend.mapper.UserVoucherMapper;
import com.multi.loyaltybackend.model.Registration;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.repository.UserRepository;
import com.multi.loyaltybackend.dto.request.ProfileUpdateRequest;
import com.multi.loyaltybackend.model.UserVoucher;
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
    private final ProfileMapper profileMapper;
    private final UserEventMapper userEventMapper;
    private final UserVoucherMapper userVoucherMapper;

    public ProfileResponse getProfile(String email) {
        User user = findUserByEmail(email);
        return profileMapper.toResponse(user);
    }

    public ProfileResponse updateProfile(String email, ProfileUpdateRequest request) {
        User user = findUserByEmail(email);

        if (request.fullName() != null) user.setFullName(request.fullName());
        if (request.age() != null) user.setAge(request.age());
        if (request.mobileNumber() != null) user.setMobileNumber(request.mobileNumber());
        if (request.aboutMe() != null) user.setAboutMe(request.aboutMe());

        return profileMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public ProfileResponse uploadProfileImage(String email, MultipartFile image) {
        User user = findUserByEmail(email);

        String newFileName = fileStorageService.storeFile(image);
        String oldFileName = user.getFileName();

        user.setFileName(newFileName);
        ProfileResponse response = profileMapper.toResponse(userRepository.save(user));

        deleteOldFileIfNeeded(oldFileName, newFileName);
        return response;
    }

    @Transactional
    public void deleteProfileImage(String email) {
        User user = findUserByEmail(email);
        String fileName = user.getFileName();

        if (fileName != null && !fileName.isEmpty()) {
            user.setFileName(null);
            userRepository.save(user);
            fileStorageService.deleteFile(fileName);
        }
    }

    public List<UserEventResponse> getUserEvents(String email) {
        User user = findUserByEmail(email);
        List<Registration> registrations = user.getRegistrations();
        return userEventMapper.toResponseList(registrations);
    }

    public List<UserVoucherResponse> getUserVouchers(String email) {
        User user = findUserByEmail(email);
        List<UserVoucher> vouchers = user.getUserVouchers();
        return userVoucherMapper.toResponseList(vouchers);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findUserByEmail(email);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidCurrentPasswordException();
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("ახალი პაროლი და განმეორებითი პაროლი არ ემთხვევა");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("მომხმარებელი ვერ მოიძებნა ელფოსტით: " + email));
    }

    private void deleteOldFileIfNeeded(String oldFile, String newFile) {
        if (oldFile != null && !oldFile.equals(newFile)) {
            fileStorageService.deleteFile(oldFile);
        }
    }
}

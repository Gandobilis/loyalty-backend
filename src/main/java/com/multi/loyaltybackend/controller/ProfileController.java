package com.multi.loyaltybackend.controller;

import com.multi.loyaltybackend.dto.request.ChangePasswordRequest;
import com.multi.loyaltybackend.dto.response.ProfileResponse;
import com.multi.loyaltybackend.dto.request.ProfileUpdateRequest;
import com.multi.loyaltybackend.dto.response.UserEventResponse;
import com.multi.loyaltybackend.dto.response.UserVoucherResponse;
import com.multi.loyaltybackend.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        ProfileResponse profile = profileService.getProfile(authentication.getName());
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(Authentication authentication, @Valid @RequestBody ProfileUpdateRequest request) {
        ProfileResponse updatedProfile = profileService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/image")
    public ResponseEntity<ProfileResponse> uploadProfileImage(Authentication authentication, @RequestParam("image") MultipartFile image) {
        ProfileResponse updatedProfile = profileService.uploadProfileImage(authentication.getName(), image);
        return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping("/image")
    public ResponseEntity<ProfileResponse> deleteProfileImage(Authentication authentication) {
        profileService.deleteProfileImage(authentication.getName());
        ProfileResponse profile = profileService.getProfile(authentication.getName());
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/events")
    public ResponseEntity<List<UserEventResponse>> getEvents(Authentication authentication) {
        List<UserEventResponse> userEvents = profileService.getUserEvents(authentication.getName());
        return ResponseEntity.ok(userEvents);
    }

    @GetMapping("/vouchers")
    public ResponseEntity<List<UserVoucherResponse>> getVouchers(Authentication authentication) {
        List<UserVoucherResponse> userVouchers = profileService.getUserVouchers(authentication.getName());
        return ResponseEntity.ok(userVouchers);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map.Entry<String, String>> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(authentication.getName(), request);

        return ResponseEntity.ok(Map.entry("message", "პაროლი შეიცვალა წარმატებით"));
    }
}
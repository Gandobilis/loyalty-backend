package com.multi.loyaltybackend.controller;

import com.multi.loyaltybackend.dto.ProfileResponse;
import com.multi.loyaltybackend.dto.ProfileUpdateRequest;
import com.multi.loyaltybackend.dto.UserEventResponse;
import com.multi.loyaltybackend.dto.UserVoucherResponse;
import com.multi.loyaltybackend.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    public ResponseEntity<ProfileResponse> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest update,
            Authentication authentication) {
        ProfileResponse updatedProfile = profileService.updateProfile(authentication.getName(), update);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/image")
    public ResponseEntity<ProfileResponse> uploadProfileImage(
            @RequestParam("image") MultipartFile image,
            Authentication authentication) {
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
}
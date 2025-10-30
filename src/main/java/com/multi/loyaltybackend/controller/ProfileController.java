package com.multi.loyaltybackend.controller;

import com.multi.loyaltybackend.dto.request.ChangePasswordRequest;
import com.multi.loyaltybackend.dto.response.*;
import com.multi.loyaltybackend.dto.request.ProfileUpdateRequest;
import com.multi.loyaltybackend.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<ProfileResponse> deleteProfileImage(Authentication authentication) {
        profileService.deleteProfileImage(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events")
    public ResponseEntity<UserEventsWithPointsResponse> getEvents(Authentication authentication) {
        UserEventsWithPointsResponse userEvents = profileService.getUserEvents(authentication.getName());
        return ResponseEntity.ok(userEvents);
    }

    @GetMapping("/vouchers")
    public ResponseEntity<UserVouchersWithPointsResponse> getVouchers(Authentication authentication) {
        UserVouchersWithPointsResponse userVouchers = profileService.getUserVouchers(authentication.getName());
        return ResponseEntity.ok(userVouchers);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map.Entry<String, String>> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(authentication.getName(), request);

        return ResponseEntity.ok(Map.entry("message", "პაროლი შეიცვალა წარმატებით"));
    }
}
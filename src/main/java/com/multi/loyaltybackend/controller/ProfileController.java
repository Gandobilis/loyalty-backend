package com.multi.loyaltybackend.controller;

import com.multi.loyaltybackend.dto.ProfileResponseDTO;
import com.multi.loyaltybackend.dto.ProfileUpdateDTO;
import com.multi.loyaltybackend.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponseDTO> getProfile(Authentication authentication) {
        ProfileResponseDTO profile = profileService.getProfile(authentication.getName());
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<ProfileResponseDTO> updateProfile(
            @Valid @RequestBody ProfileUpdateDTO updateDTO,
            Authentication authentication) {
        ProfileResponseDTO updatedProfile = profileService.updateProfile(authentication.getName(), updateDTO);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/image")
    public ResponseEntity<ProfileResponseDTO> uploadProfileImage(
            @RequestParam("image") MultipartFile image,
            Authentication authentication) {
        ProfileResponseDTO updatedProfile = profileService.uploadProfileImage(authentication.getName(), image);
        return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping("/image")
    public ResponseEntity<ProfileResponseDTO> deleteProfileImage(Authentication authentication) {
        profileService.deleteProfileImage(authentication.getName());
        ProfileResponseDTO profile = profileService.getProfile(authentication.getName());
        return ResponseEntity.ok(profile);
    }
}
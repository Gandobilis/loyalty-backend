package com.multi.loyaltybackend.registration.controller;

import com.multi.loyaltybackend.registration.dto.RegistrationRequest;
import com.multi.loyaltybackend.registration.dto.RegistrationResponse;
import com.multi.loyaltybackend.registration.dto.StatusUpdateRequest;
import com.multi.loyaltybackend.registration.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<RegistrationResponse> registerUser(@Valid @RequestBody RegistrationRequest request) {
        RegistrationResponse response = registrationService.registerUserToEvent(
                request.userId(),
                request.eventId(),
                request.comment()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{registrationId}/status")
    public ResponseEntity<RegistrationResponse> updateStatus(
            @PathVariable Long registrationId,
            @Valid @RequestBody StatusUpdateRequest request) {
        RegistrationResponse response = registrationService.updateRegistrationStatus(
                registrationId,
                request.status()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{registrationId}")
    public ResponseEntity<Void> cancelRegistration(@PathVariable Long registrationId) {
        registrationService.cancelRegistration(registrationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{registrationId}")
    public ResponseEntity<RegistrationResponse> getRegistration(@PathVariable Long registrationId) {
        RegistrationResponse response = registrationService.getRegistration(registrationId);
        return ResponseEntity.ok(response);
    }
}
package com.multi.loyaltybackend.auth.dto;

public record PasswordUpdateRequest(String token, String newPassword) {
}
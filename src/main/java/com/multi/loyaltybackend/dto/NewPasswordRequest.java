package com.multi.loyaltybackend.dto;

public record NewPasswordRequest(String token, String newPassword) {}
package com.multi.loyaltybackend.auth.dto;

import com.multi.loyaltybackend.auth.model.Role;

public record RegisterRequest(String email, Role role, String fullName, String password) {
}
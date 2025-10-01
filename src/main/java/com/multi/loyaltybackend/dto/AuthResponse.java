package com.multi.loyaltybackend.dto;

/**
 * Represents the authentication response payload containing the access token.
 *
 * @param token The JWT (JSON Web Token) or other access token for the authenticated user.
 *              This token cannot be null or blank.
 */
public record AuthResponse(String token) {
    /**
     * Compact constructor to validate that the token is not null or blank.
     *
     * @throws IllegalArgumentException if the token is null or blank.
     */
    public AuthResponse {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or blank.");
        }
    }
}
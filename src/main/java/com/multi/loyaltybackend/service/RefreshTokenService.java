package com.multi.loyaltybackend.service;

import com.multi.loyaltybackend.exception.InvalidRefreshTokenException;
import com.multi.loyaltybackend.exception.RefreshTokenExpiredException;
import com.multi.loyaltybackend.model.RefreshToken;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshTokenDurationMs;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Delete any existing refresh token for this user
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenExpiredException("Refresh token has expired. Please login again.");
        }
        return token;
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));
    }

    @Transactional
    public void revokeToken(String token) {
        RefreshToken refreshToken = findByToken(token);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void revokeUserTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    public void validateToken(RefreshToken token) {
        if (token.isRevoked()) {
            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }
        verifyExpiration(token);
    }
}

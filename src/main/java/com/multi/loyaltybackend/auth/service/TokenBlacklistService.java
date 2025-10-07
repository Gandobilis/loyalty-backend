package com.multi.loyaltybackend.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private static final String BLOCKLIST_KEY_PREFIX = "jwt:blocklist:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public void addToBlocklist(String token) {
        long remainingValidity = getRemainingValidityMillis(token);

        if (remainingValidity > 0) {
            String key = BLOCKLIST_KEY_PREFIX + token;
            redisTemplate.opsForValue().set(key, "blocked", remainingValidity, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isTokenBlocklisted(String token) {
        String key = BLOCKLIST_KEY_PREFIX + token;
        return redisTemplate.hasKey(key);
    }

    private long getRemainingValidityMillis(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            if (expiration == null) {
                return 0;
            }

            long diff = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, diff);
        } catch (Exception e) {
            return 0;
        }
    }
}
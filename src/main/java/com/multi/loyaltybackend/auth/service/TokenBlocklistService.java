package com.multi.loyaltybackend.auth.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class TokenBlocklistService {
    // In a production environment, use a distributed cache like Redis
    private Set<String> blocklist = Collections.synchronizedSet(new HashSet<>());

    public void blocklistToken(String token) {
        blocklist.add(token);
    }

    public boolean isTokenBlocklisted(String token) {
        return blocklist.contains(token);
    }
}
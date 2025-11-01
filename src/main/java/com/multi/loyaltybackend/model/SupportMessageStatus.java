package com.multi.loyaltybackend.model;

public enum SupportMessageStatus {
    OPEN,         // Message created, waiting for support
    IN_PROGRESS,  // Support is working on it
    RESOLVED,     // Support has responded, resolved
    CLOSED        // Message closed (by user or admin)
}

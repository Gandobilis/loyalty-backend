package com.multi.loyaltybackend.chat.model;

/**
 * Enum representing the type of chat message.
 */
public enum MessageType {
    /**
     * Regular text message
     */
    TEXT,

    /**
     * Message with file attachment
     */
    FILE,

    /**
     * System generated message (e.g., "Chat closed", "Agent joined")
     */
    SYSTEM,

    /**
     * Image attachment
     */
    IMAGE
}

package com.multi.loyaltybackend.chat.model;

/**
 * Enum representing the status of a support chat session.
 */
public enum ChatStatus {
    /**
     * Chat has been opened by user, waiting for admin response
     */
    OPEN,

    /**
     * Chat is actively being handled by support
     */
    ACTIVE,

    /**
     * Chat has been resolved and closed
     */
    CLOSED,

    /**
     * Chat has been reopened after being closed
     */
    REOPENED
}

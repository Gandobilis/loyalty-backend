package com.multi.loyaltybackend.config;

/**
 * Constants for logging identifiers across the application
 */
public final class LoggingConstants {

    private LoggingConstants() {
        // Prevent instantiation
    }

    // Application identifiers
    public static final String ADMIN_PANEL = "[ADMIN-PANEL]";
    public static final String API = "[API]";
    public static final String SYSTEM = "[SYSTEM]";

    // Operation types
    public static final String CREATE = "CREATE";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String READ = "READ";
    public static final String APPROVE = "APPROVE";
    public static final String COMPLETE = "COMPLETE";
    public static final String CANCEL = "CANCEL";
    public static final String AWARD_POINTS = "AWARD_POINTS";

    // Entity types
    public static final String USER_ENTITY = "User";
    public static final String EVENT_ENTITY = "Event";
    public static final String REGISTRATION_ENTITY = "Registration";
    public static final String VOUCHER_ENTITY = "Voucher";
    public static final String COMPANY_ENTITY = "Company";

    /**
     * Formats a log message with operation context
     *
     * @param appId Application identifier (ADMIN_PANEL or API)
     * @param operation Operation type
     * @param entity Entity type
     * @param entityId Entity ID
     * @param userId User performing the action (optional)
     * @return Formatted log prefix
     */
    public static String formatLog(String appId, String operation, String entity, Long entityId, String userId) {
        StringBuilder sb = new StringBuilder();
        sb.append(appId).append(" ");
        sb.append("[").append(operation).append("] ");
        sb.append(entity);
        if (entityId != null) {
            sb.append(" ID=").append(entityId);
        }
        if (userId != null && !userId.isEmpty()) {
            sb.append(" by User=").append(userId);
        }
        return sb.toString();
    }

    /**
     * Formats a log message without user context
     */
    public static String formatLog(String appId, String operation, String entity, Long entityId) {
        return formatLog(appId, operation, entity, entityId, null);
    }
}

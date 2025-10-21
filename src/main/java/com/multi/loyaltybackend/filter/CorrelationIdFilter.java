package com.multi.loyaltybackend.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that generates and manages correlation IDs for request tracking.
 * The correlation ID is:
 * - Generated for each request if not provided
 * - Added to the MDC (Mapped Diagnostic Context) for logging
 * - Returned in the response header for client tracking
 * - Used for end-to-end request tracing
 *
 * Correlation IDs can be provided by clients via the X-Correlation-ID header,
 * or will be auto-generated if not present.
 */
@Component
@Order(1)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Get correlation ID from request header or generate a new one
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = generateCorrelationId();
            }

            // Store correlation ID in MDC for logging
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

            // Add correlation ID to response header
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            // Continue with the filter chain
            filterChain.doFilter(request, response);

        } finally {
            // Clean up MDC after request processing
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    /**
     * Generates a unique correlation ID.
     *
     * @return A UUID-based correlation ID
     */
    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Gets the current correlation ID from the MDC.
     *
     * @return The current correlation ID, or null if not set
     */
    public static String getCurrentCorrelationId() {
        return MDC.get(CORRELATION_ID_MDC_KEY);
    }
}

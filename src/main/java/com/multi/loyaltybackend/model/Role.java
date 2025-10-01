package com.multi.loyaltybackend.model;

import org.springframework.security.core.GrantedAuthority;

/**
 * Defines the user roles within the application.
 * <p>
 * This enum implements Spring Security's {@link GrantedAuthority} interface,
 * allowing it to be used directly in security contexts.
 */
public enum Role implements GrantedAuthority {

    ROLE_USER,
    ROLE_ADMIN;

    /**
     * Returns the string representation of the role, which is required
     * by the {@link GrantedAuthority} interface.
     *
     * @return The name of the enum constant (e.g., "ROLE_USER").
     */
    @Override
    public String getAuthority() {
        return name();
    }
}
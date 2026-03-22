package com.kaijie.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security helper utilities
 */
public class SecurityUtils {

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();
        // In our setup we stored username as the principal (String) in JwtAuthenticationFilter
        if (principal instanceof String) return (String) principal;
        // Fallback to authentication name
        return authentication.getName();
    }
}


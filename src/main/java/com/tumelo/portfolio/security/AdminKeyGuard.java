package com.tumelo.portfolio.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import com.tumelo.portfolio.exception.AdminEndpointDisabledException;
import com.tumelo.portfolio.exception.UnauthorizedAdminException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdminKeyGuard {

    private final String configuredKey;

    public AdminKeyGuard(
            @Value("${app.admin-api-key:}") String configuredKey
    ) {
        this.configuredKey = configuredKey == null ? "" : configuredKey.trim();
    }

    public void verify(String suppliedKey) {
        if (configuredKey.isBlank()) {
            throw new AdminEndpointDisabledException(
                    "Admin endpoints are disabled because APP_ADMIN_API_KEY is not configured"
            );
        }

        byte[] expected = configuredKey.getBytes(StandardCharsets.UTF_8);
        byte[] supplied = (suppliedKey == null ? "" : suppliedKey)
                .getBytes(StandardCharsets.UTF_8);

        if (!MessageDigest.isEqual(expected, supplied)) {
            throw new UnauthorizedAdminException("Invalid admin API key");
        }
    }
}

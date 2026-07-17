package com.tumelo.portfolio.security;

import com.tumelo.portfolio.exception.AdminEndpointDisabledException;
import com.tumelo.portfolio.exception.UnauthorizedAdminException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminKeyGuardTest {

    @Test
    void acceptsTheConfiguredKey() {
        AdminKeyGuard guard = new AdminKeyGuard("correct-key");

        assertDoesNotThrow(() -> guard.verify("correct-key"));
    }

    @Test
    void rejectsAnIncorrectKey() {
        AdminKeyGuard guard = new AdminKeyGuard("correct-key");

        assertThrows(
                UnauthorizedAdminException.class,
                () -> guard.verify("wrong-key")
        );
    }

    @Test
    void disablesAdminEndpointsWhenNoKeyIsConfigured() {
        AdminKeyGuard guard = new AdminKeyGuard("");

        assertThrows(
                AdminEndpointDisabledException.class,
                () -> guard.verify("anything")
        );
    }
}

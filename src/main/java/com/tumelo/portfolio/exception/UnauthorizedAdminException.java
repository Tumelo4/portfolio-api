package com.tumelo.portfolio.exception;

public class UnauthorizedAdminException extends RuntimeException {

    public UnauthorizedAdminException(String message) {
        super(message);
    }
}

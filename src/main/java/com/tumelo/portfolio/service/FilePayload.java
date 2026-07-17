package com.tumelo.portfolio.service;

import org.springframework.http.MediaType;

public record FilePayload(
        byte[] content,
        MediaType mediaType,
        String filename
) {
}

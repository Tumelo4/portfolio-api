package com.tumelo.portfolio.dto;

public record FileUploadResponse(
        String id,
        String filename,
        String mediaType,
        long size,
        String message
) {
}

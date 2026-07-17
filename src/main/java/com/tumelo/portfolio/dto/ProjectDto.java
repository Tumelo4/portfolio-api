package com.tumelo.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProjectDto(
        @NotBlank
        @Size(max = 120)
        String title,

        @NotBlank
        @Size(max = 2000)
        String description,

        @NotBlank
        @Size(max = 100)
        String imageId,

        @NotBlank
        @Pattern(
                regexp = "^https?://.+$",
                message = "link must start with http:// or https://"
        )
        @Size(max = 500)
        String link
) {
}

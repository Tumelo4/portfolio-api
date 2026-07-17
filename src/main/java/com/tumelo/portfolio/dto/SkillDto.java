package com.tumelo.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SkillDto(
        @NotBlank
        @Size(max = 80)
        String name,

        @NotBlank
        @Size(max = 100)
        String imageId
) {
}

package com.tumelo.portfolio.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PortfolioRequest(
        @NotBlank
        @Size(max = 80)
        String name,

        @NotBlank
        @Size(max = 80)
        String surname,

        @NotBlank
        @Size(max = 160)
        String occupation,

        @Pattern(
                regexp = "^$|^https?://.+$",
                message = "linkedinLink must be empty or start with http:// or https://"
        )
        @Size(max = 500)
        String linkedinLink,

        @Pattern(
                regexp = "^$|^https?://.+$",
                message = "githubLink must be empty or start with http:// or https://"
        )
        @Size(max = 500)
        String githubLink,

        @Pattern(
                regexp = "^$|^https?://.+$",
                message = "instagramLink must be empty or start with http:// or https://"
        )
        @Size(max = 500)
        String instagramLink,

        @Size(max = 100)
        String pdfResumeId,

        @Size(max = 100)
        String imageId,

        @JsonProperty("home_description")
        @NotBlank
        @Size(max = 4000)
        String homeDescription,

        @JsonProperty("about_description")
        @NotBlank
        @Size(max = 8000)
        String aboutDescription,

        @NotNull
        List<@Valid SkillDto> skills,

        @NotNull
        List<@Valid ProjectDto> projects
) {
}

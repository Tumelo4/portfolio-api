package com.tumelo.portfolio.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PortfolioResponse(
        String id,
        String name,
        String surname,
        String occupation,
        String linkedinLink,
        String githubLink,
        String instagramLink,
        String pdfResumeId,
        String imageId,

        @JsonProperty("home_description")
        String homeDescription,

        @JsonProperty("about_description")
        String aboutDescription,

        List<SkillDto> skills,
        List<ProjectDto> projects
) {
}

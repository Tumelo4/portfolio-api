package com.tumelo.portfolio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank
        @Size(max = 80)
        String firstName,

        @NotBlank
        @Size(max = 80)
        String lastName,

        @NotBlank
        @Email
        @Size(max = 254)
        String workEmail,

        @NotBlank
        @Pattern(
                regexp = "^https?://.+$",
                message = "workUrl must start with http:// or https://"
        )
        @Size(max = 500)
        String workUrl,

        @NotBlank
        @Size(max = 160)
        String jobTitle,

        @Pattern(
                regexp = "^$|^[+0-9()\\-\\s]{7,30}$",
                message = "contactNumber contains unsupported characters"
        )
        String contactNumber,

        @NotBlank
        @Size(min = 10, max = 4000)
        String message
) {
}

package ua.nin.identity.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResendVerifyRequest(
        @Email @NotBlank @Size(max = 64) String email
) {}

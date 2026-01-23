package ua.nin.identity.profile.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ua.nin.identity.profile.model.Privacy;

public record UpdateProfileRequest(

        @Size(min = 3, max = 64)
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can contain letters, digits, underscore")
        String username,

        @Size(max = 64)
        String displayName,

        @Size(max = 500)
        String bio,

        Privacy privacy,

        @Size(max = 16)
        String locale,

        @Size(max = 64)
        String timezone
) {}
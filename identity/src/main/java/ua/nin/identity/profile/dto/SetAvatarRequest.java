package ua.nin.identity.profile.dto;

import jakarta.validation.constraints.NotNull;

public record SetAvatarRequest(
        @NotNull Long mediaId
) {}
package ua.nin.identity.profile.dto;

import ua.nin.identity.profile.model.Privacy;

import java.time.Instant;

public record ProfileResponse(
        long userId,
        String username,
        String displayName,
        String bio,
        Privacy privacy,
        String locale,
        String timezone,
        Instant createdAt,
        Instant updatedAt
) {}

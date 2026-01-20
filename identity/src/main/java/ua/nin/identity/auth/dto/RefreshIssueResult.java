package ua.nin.identity.auth.dto;

public record RefreshIssueResult(
        String rawRefreshToken,
        long familyId
) {}

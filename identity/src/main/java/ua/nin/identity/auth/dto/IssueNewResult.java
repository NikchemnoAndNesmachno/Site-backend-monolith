package ua.nin.identity.auth.dto;

public record IssueNewResult(
        String rawRefreshToken,
        long familyId
) {}

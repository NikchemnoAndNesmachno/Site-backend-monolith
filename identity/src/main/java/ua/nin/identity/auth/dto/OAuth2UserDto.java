package ua.nin.identity.auth.dto;

import lombok.Builder;
import ua.nin.identity.auth.model.Provider;

@Builder
public record OAuth2UserDto(
        Provider provider,
        String providerId,
        String email,
        boolean emailVerified,
        String name,
        String picture
) {
}

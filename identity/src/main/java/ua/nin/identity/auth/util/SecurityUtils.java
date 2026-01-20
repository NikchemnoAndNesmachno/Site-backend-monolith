package ua.nin.identity.auth.util;

import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@NoArgsConstructor
public final class SecurityUtils {

    // TODO: change to auth provider, so user id could be extracted from Authentication Principal in controller layer
    public static long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated principal");
        }
        // Для JWT resource server principal зазвичай це Jwt
        // але простіше й стабільніше брати name (він = sub)
        String sub = auth.getName();
        return Long.parseLong(sub);
    }
}
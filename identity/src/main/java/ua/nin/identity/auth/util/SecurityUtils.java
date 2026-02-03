package ua.nin.identity.auth.util;

import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// Left for later deletion
@NoArgsConstructor
@Deprecated(forRemoval = true)
/**
 * @deprecated
 */
public final class SecurityUtils {


    @Deprecated
    /**
     * @deprecated (changed to Authentication (Principal) based JWT decoding in controllers
     */
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
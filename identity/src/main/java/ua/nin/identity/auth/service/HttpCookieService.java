package ua.nin.identity.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class HttpCookieService {
    public static final String REFRESH_COOKIE = "refresh_token";

    // TODO: поставити собі ці значення з config
    private static final int REFRESH_TTL_SECONDS = 60 * 60 * 24 * 14; // 14 days
    // у dev можна false
    private static final boolean HTTP_COOKIE_SECURE = false;

    public void setRefreshCookie(HttpServletResponse response, String token) {

        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true)
                .secure(HTTP_COOKIE_SECURE)
                .path("/api/v1/auth/refresh")
                .maxAge(REFRESH_TTL_SECONDS)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(HTTP_COOKIE_SECURE)
                .path("/api/v1/auth/refresh")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}

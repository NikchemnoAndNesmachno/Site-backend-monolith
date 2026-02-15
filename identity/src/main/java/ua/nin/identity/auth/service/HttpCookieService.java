package ua.nin.identity.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class HttpCookieService {
    public static final String REFRESH_COOKIE = "refresh_token";

    @Value("${security.refresh.ttl-days:14}")
    private long refreshTtlDays;

    @Value("${security.refresh.secure}")
    private boolean httpCookieSecure = false;

    public void setRefreshCookie(HttpServletResponse response, String token) {
        long refreshTtlSeconds = refreshTtlDays * 24 * 60 * 60;

        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true)
                .secure(httpCookieSecure)
                .path("/api/v1/auth/refresh")
                .maxAge(refreshTtlSeconds)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(httpCookieSecure)
                .path("/api/v1/auth/refresh")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}

package ua.nin.identity.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class HttpCookieService {
    public static final String REFRESH_COOKIE = "refresh_token";
    public static final String REFRESH_TOKEN_PATH = "/api/v1/auth/refresh";

    private final long refreshTtlDays;
    private final boolean httpCookieSecure;

    public HttpCookieService (@Value("${security.refresh.ttl-days:14}") Long refreshTtlDays,
                              @Value("${security.refresh.secure}") Boolean httpCookieSecure) {
        this.refreshTtlDays = refreshTtlDays;
        this.httpCookieSecure = httpCookieSecure;
    }

    public void setRefreshCookie(HttpServletResponse response, String token) {
        long refreshTtlSeconds = refreshTtlDays * 24 * 60 * 60;

        addCookie(response, REFRESH_COOKIE, token, REFRESH_TOKEN_PATH, refreshTtlSeconds);
    }

    public void clearRefreshCookie(HttpServletResponse response) {
        deleteCookie(response, REFRESH_COOKIE, REFRESH_TOKEN_PATH);
    }

    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }

    public void addCookie(HttpServletResponse response, String name, String value, String path, long maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(httpCookieSecure)
                .path(path)
                .sameSite("Lax")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void deleteCookie(HttpServletResponse response, String name, String path) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(httpCookieSecure)
                .path(path)
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}

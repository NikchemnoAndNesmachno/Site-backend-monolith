package ua.nin.identity.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ua.nin.identity.auth.dto.*;
import ua.nin.identity.auth.service.AuthService;
import ua.nin.identity.auth.service.HttpCookieService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final HttpCookieService cookieService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest req) {
        log.debug("Auth register email={}", req.email());
        authService.register(req);
        // 201 без body — нормально. Можеш повернути “ok/next steps”
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req,
                                              HttpServletRequest request,
                                              HttpServletResponse response,
                                              @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        log.debug("Auth login email={}", req.email());
        AuthResult result = authService.login(req, userAgent, request.getRemoteAddr());
        cookieService.setRefreshCookie(response, result.refreshToken()); // HttpOnly cookie
        return ResponseEntity.ok(result.authResponse()); // access в JSON
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = HttpCookieService.REFRESH_COOKIE, required = false) String refreshToken,
                                                HttpServletRequest request,
                                                HttpServletResponse response,
                                                @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        log.debug("Auth refresh requested");
        AuthResult result = authService.refresh(refreshToken, userAgent, request.getRemoteAddr());
        cookieService.setRefreshCookie(response, result.refreshToken()); // rotation
        return ResponseEntity.ok(result.authResponse());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = HttpCookieService.REFRESH_COOKIE, required = false) String refreshToken,
                                       HttpServletResponse response) {
        log.debug("Auth logout requested");
        authService.logout(refreshToken);
        cookieService.clearRefreshCookie(response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(Authentication authentication) {
        long userId = Long.parseLong(authentication.getName());
        log.debug("Authenticated userId={} requested logout all", userId);
        authService.logoutAll(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication) {
        long userId = Long.parseLong(authentication.getName());
        log.debug("Authenticated userId={} requested auth me", userId);
        return ResponseEntity.ok(authService.me(userId));
    }
}
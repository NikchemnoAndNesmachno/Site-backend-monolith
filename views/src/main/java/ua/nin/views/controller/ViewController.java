package ua.nin.views.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ua.nin.views.dto.ViewCountsResponse;
import ua.nin.views.service.ViewService;

@RestController
@RequestMapping("/api/v1/views")
@RequiredArgsConstructor
@Slf4j
public class ViewController {

    private final ViewService viewService;

    @PostMapping
    public ResponseEntity<Void> recordView(
            @RequestParam @NotBlank String targetType,
            @RequestParam long targetId,
            Authentication auth,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "X-Forwarded-For", required = false) String xff,
            @RequestHeader(value = "X-Real-IP", required = false) String xRealIp
    ) {
        log.debug("Record view endpoint targetType={}, targetId={}", targetType, targetId);
        Long userId = null;
        if (auth != null && auth.isAuthenticated()) {
            userId = Long.parseLong(auth.getName());
        }

        String ip = extractIp(xff, xRealIp);
        viewService.recordView(targetType, targetId, userId, userAgent, ip);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ViewCountsResponse> viewCountsForTarget(
            @RequestParam @NotBlank String targetType,
            @RequestParam long targetId
    ) {
        log.debug("View counts endpoint targetType={}, targetId={}", targetType, targetId);
        return ResponseEntity.ok(viewService.getCounts(targetType, targetId));
    }

    private static String extractIp(String xff, String xRealIp) {
        if (xff != null && !xff.isBlank()) {
            // беремо перший IP у XFF
            String first = xff.split(",")[0].trim();
            if (!first.isEmpty()) return first;
        }
        if (xRealIp != null && !xRealIp.isBlank()) return xRealIp.trim();
        return null;
    }
}

package ua.nin.feed.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.nin.contract.feed.FeedSort;
import ua.nin.feed.dto.FeedPageResponse;
import ua.nin.feed.service.FeedService;

@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
@Validated
@Slf4j
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public ResponseEntity<FeedPageResponse> getFeed(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "8") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "LATEST") FeedSort sort,
            Authentication authentication
    ) {
        Long currentUserId = extractCurrentUserId(authentication);
        log.debug("Getting feed for page {} by user: {}", page, currentUserId);

        return ResponseEntity.ok(
                feedService.getFeed(page, size, sort, currentUserId)
        );
    }

    private Long extractCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        try {
            return Long.parseLong(authentication.getName());
        } catch (Exception e) {
            return null;
        }
    }
}
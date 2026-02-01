package ua.nin.reactions.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ua.nin.reactions.dto.PutReactionRequest;
import ua.nin.reactions.dto.ReactionActionResponse;
import ua.nin.reactions.service.ReactionService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @PutMapping
    public ResponseEntity<ReactionActionResponse> put(Authentication authentication,
                                                      @Valid @RequestBody PutReactionRequest req) {
        long userId = Long.parseLong(authentication.getName()); // sub=userId
        return ResponseEntity.ok(reactionService.put(userId, req));
    }

    @GetMapping("/{targetType}/{targetId}/counts")
    public ResponseEntity<Map<String, Long>> counts(@PathVariable String targetType,
                                                    @PathVariable long targetId) {
        return ResponseEntity.ok(reactionService.counts(targetType, targetId));
    }

    @GetMapping("/{targetType}/{targetId}/my")
    public ResponseEntity<String> my(Authentication authentication,
                                     @PathVariable String targetType,
                                     @PathVariable long targetId) {
        long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(reactionService.myReaction(userId, targetType, targetId));
    }
}

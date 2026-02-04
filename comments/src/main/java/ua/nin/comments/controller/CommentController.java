package ua.nin.comments.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ua.nin.comments.dto.*;
import ua.nin.comments.service.CommentService;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> create(Authentication auth, @Valid @RequestBody CreateCommentRequest req) {
        long userId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(commentService.create(userId, req));
    }

    @GetMapping
    public ResponseEntity<Page<CommentResponse>> listRoot(@RequestParam @NotNull String targetType,
                                                          @RequestParam @NotNull @Positive Long targetId,
                                                          @PageableDefault(page = 0, size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(commentService.listRoot(targetType, targetId, pageable));
    }

    @GetMapping("/{parentId}/replies")
    public ResponseEntity<Page<CommentResponse>> listReplies(@PathVariable long parentId,
                                                             @PageableDefault(page = 0, size = 5) Pageable pageable) {
        return ResponseEntity.ok(commentService.listReplies(parentId, pageable));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> update(Authentication auth,
                                                  @PathVariable long commentId,
                                                  @Valid @RequestBody UpdateCommentRequest req) {
        long userId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(commentService.update(userId, commentId, req));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable long commentId) {
        long userId = Long.parseLong(auth.getName());
        commentService.delete(userId, commentId);
        return ResponseEntity.noContent().build();
    }
}

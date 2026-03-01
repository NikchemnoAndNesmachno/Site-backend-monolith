package ua.nin.media.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.nin.media.dto.AvatarUploadResponse;
import ua.nin.media.service.UserAvatarService;

@RestController
@RequestMapping("/api/v1/avatar")
@RequiredArgsConstructor
@Slf4j
public class UserAvatarController {

    private final UserAvatarService userAvatarService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AvatarUploadResponse> uploadVideoWithPreview(
            Authentication authentication,
            @RequestPart("avatar") MultipartFile avatar
    ) {
        long userId = Long.parseLong(authentication.getName());
        log.debug("Authenticated userId={} requested avatar upload", userId);
        return ResponseEntity.ok(userAvatarService.uploadAvatar(userId, avatar));
    }

    @DeleteMapping("/{avatarId}")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable long avatarId
    ) {
        long userId = Long.parseLong(authentication.getName());
        log.debug("Authenticated userId={} requested avatar deletion", userId);
        userAvatarService.deleteAvatar(userId, avatarId);
        return ResponseEntity.noContent().build();
    }
}

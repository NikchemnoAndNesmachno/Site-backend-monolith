package ua.nin.media.controller;

import lombok.RequiredArgsConstructor;
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
public class UserAvatarController {

    private final UserAvatarService userAvatarService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AvatarUploadResponse> uploadVideoWithPreview(
            @RequestPart("avatar") MultipartFile avatar,
            Authentication authentication
    ) {
        long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(userAvatarService.uploadAvatar(userId, avatar));
    }

    @DeleteMapping("/{avatarId}")
    public ResponseEntity<Void> delete(@PathVariable long avatarId, Authentication authentication) {
        long userId = Long.parseLong(authentication.getName());
        userAvatarService.deleteAvatar(userId, avatarId);
        return ResponseEntity.noContent().build();
    }
}

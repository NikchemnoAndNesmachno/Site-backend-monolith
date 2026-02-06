package ua.nin.media.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.nin.media.dto.VideoWithPreviewUploadResponse;
import ua.nin.media.model.VideoVisibility;
import ua.nin.media.service.VideoService;

@RestController
@RequestMapping("/api/v1/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @PostMapping(value = "/upload/video-with-preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoWithPreviewUploadResponse> uploadVideoWithPreview(
            @NotNull @Validated @RequestPart("title") String title,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "visibility", required = false) VideoVisibility visibility,
            @RequestPart("video") MultipartFile video,
            @RequestPart("preview") MultipartFile preview,
            Authentication authentication
    ) {
        long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(videoService.uploadVideoWithPreview(title, description, visibility, userId, video, preview));
    }

    @DeleteMapping("/{videoId}")
    public ResponseEntity<Void> delete(@PathVariable long videoId, Authentication authentication) {
        long userId = Long.parseLong(authentication.getName());
        videoService.deleteVideoWithPreview(userId, videoId);
        return ResponseEntity.noContent().build();
    }
}

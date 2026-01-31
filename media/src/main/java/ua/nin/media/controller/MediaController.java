package ua.nin.media.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.nin.media.dto.MediaMetaResponse;
import ua.nin.media.dto.MediaUploadResponse;
import ua.nin.media.model.MediaAsset;
import ua.nin.media.service.MediaService;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> upload(
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) {
        long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(mediaService.uploadAsset(userId, file));
    }

    @GetMapping("/{id}/meta")
    public ResponseEntity<MediaMetaResponse> meta(@PathVariable long id) {
        return ResponseEntity.ok(mediaService.metaInformation(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InputStreamResource> download(@PathVariable long id) {
        MediaAsset asset = mediaService.getAssetOrThrow(id);

        InputStreamResource body = new InputStreamResource(mediaService.open(id));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeFilename(asset.getOriginalFilename(), id) + "\"")
                .contentType(MediaType.parseMediaType(asset.getContentType()))
                .contentLength(asset.getSizeBytes())
                .body(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id, Authentication authentication) {
        mediaService.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }

    private static String safeFilename(String originalFilename, long id) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "media-" + id;
        }
        // prevent header injection
        return originalFilename.replace("\r", "").replace("\n", "");
    }
}

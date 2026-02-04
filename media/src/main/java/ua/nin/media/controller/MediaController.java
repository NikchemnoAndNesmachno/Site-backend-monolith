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
            Authentication authentication,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(mediaService.uploadAsset(file));
    }

    @GetMapping("/{mediaId}/meta")
    public ResponseEntity<MediaMetaResponse> meta(@PathVariable long mediaId) {
        return ResponseEntity.ok(mediaService.metaInformation(mediaId));
    }

    @GetMapping("/{mediaId}")
    public ResponseEntity<InputStreamResource> download(@PathVariable long mediaId) {
        MediaAsset asset = mediaService.getAssetOrThrow(mediaId);

        InputStreamResource body = new InputStreamResource(mediaService.open(mediaId));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeFilename(asset.getOriginalFilename(), mediaId) + "\"")
                .contentType(MediaType.parseMediaType(asset.getContentType()))
                .contentLength(asset.getSizeBytes())
                .body(body);
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> delete(Authentication authentication,
                                       @PathVariable long mediaId) {
        mediaService.deleteAsset(mediaId);
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

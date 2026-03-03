package ua.nin.media.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.nin.media.dto.MediaMetaResponse;
import ua.nin.media.dto.MediaUploadResponse;
import ua.nin.media.model.MediaAsset;
import ua.nin.media.service.MediaService;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Slf4j
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> upload(
            @RequestPart("file") MultipartFile file
    ) {
        log.debug("Request to upload file: {}", file.getOriginalFilename());
        return ResponseEntity.ok(mediaService.uploadAsset(file));
    }

    @GetMapping("/{mediaId}/meta")
    public ResponseEntity<MediaMetaResponse> meta(@PathVariable long mediaId) {
        log.debug("Request to get meta by ID: {}", mediaId);
        return ResponseEntity.ok(mediaService.metaInformation(mediaId));
    }

    @GetMapping("/{mediaId}")
    public ResponseEntity<InputStreamResource> download(@PathVariable long mediaId) {
        log.debug("Request to download meta by ID: {}", mediaId);
        MediaAsset asset = mediaService.getAssetOrThrow(mediaId);

        InputStreamResource body = new InputStreamResource(mediaService.open(mediaId));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeFilename(asset.getOriginalFilename(), mediaId) + "\"")
                .contentType(MediaType.parseMediaType(asset.getContentType()))
                .contentLength(asset.getSizeBytes())
                .body(body);
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> delete(@PathVariable long mediaId) {
        log.debug("Request to delete media by ID: {}", mediaId);
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

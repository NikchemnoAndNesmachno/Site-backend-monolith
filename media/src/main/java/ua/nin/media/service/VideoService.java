package ua.nin.media.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ua.nin.media.dto.VideoWithPreviewUploadResponse;
import ua.nin.media.model.*;
import ua.nin.media.repository.VideoRepository;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final MediaService mediaService;

    @Transactional
    public VideoWithPreviewUploadResponse uploadVideoWithPreview(String title, String description, VideoVisibility visibility, long ownerUserId, MultipartFile videoFile, MultipartFile previewFile) {
        MediaBundle bundle = mediaService.createBundle(ownerUserId, BundleType.VIDEO_WITH_PREVIEW);

        Video createdVideo = videoRepository.save(Video.builder()
                .ownerUserId(ownerUserId)
                .mediaBundle(bundle)
                .title(title)
                .description(description)
                .visibility(visibility != null
                        ? visibility
                        : VideoVisibility.PUBLIC)
                .status(VideoStatus.UPLOADING)
                .build());

        MediaAsset videoAsset = mediaService.storeSingle(videoFile, MediaKind.VIDEO);
        MediaAsset previewAsset = mediaService.storeSingle(previewFile, MediaKind.PREVIEW);

        mediaService.createBundleItem(bundle.getId(), BundleItemRole.VIDEO, videoAsset.getId());
        mediaService.createBundleItem(bundle.getId(), BundleItemRole.PREVIEW, previewAsset.getId());

        createdVideo.setStatus(VideoStatus.READY);

        return VideoWithPreviewUploadResponse.builder()
                .videoId(createdVideo.getId())
                .bundleId(bundle.getId())
                .videoAssetId(videoAsset.getId())
                .previewAssetId(previewAsset.getId())
                .build();
    }

    @Transactional
    public void deleteVideoWithPreview(long userId, long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found"));

        if (!(video.getOwnerUserId() == userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this video");
        }

        videoRepository.delete(video);
    }
}

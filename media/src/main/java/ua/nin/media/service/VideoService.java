package ua.nin.media.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ua.nin.media.dto.VideoWithPreviewUploadResponse;
import ua.nin.media.exception.exceptions.VideoForbiddenDeletionException;
import ua.nin.media.exception.exceptions.VideoNotFound;
import ua.nin.media.model.*;
import ua.nin.media.repository.VideoRepository;

import static ua.nin.common.constant.ErrorMessage.USER_NOT_ALLOWED_TO_DELETE_VIDEO;
import static ua.nin.common.constant.ErrorMessage.VIDEO_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
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
                .orElseThrow(() -> new VideoNotFound(VIDEO_NOT_FOUND));

        if (video.getOwnerUserId() != userId) {
            throw new VideoForbiddenDeletionException(USER_NOT_ALLOWED_TO_DELETE_VIDEO);
        }

        videoRepository.delete(video);
    }
}

package ua.nin.media.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ua.nin.media.dto.VideoWithPreviewUploadResponse;
import ua.nin.media.model.*;
import ua.nin.media.repository.VideoRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock
    private VideoRepository videoRepository;
    @Mock
    private MediaService mediaService;

    @InjectMocks
    private VideoService videoService;

    @Test
    void uploadVideoWithPreview_createsBundleAndAssets() {
        MediaBundle bundle = MediaBundle.builder().id(10L).build();
        when(mediaService.createBundle(1L, BundleType.VIDEO_WITH_PREVIEW)).thenReturn(bundle);

        Video created = Video.builder().id(11L).status(VideoStatus.UPLOADING).build();
        when(videoRepository.save(any(Video.class))).thenReturn(created);

        MediaAsset videoAsset = MediaAsset.builder().id(20L).build();
        MediaAsset previewAsset = MediaAsset.builder().id(30L).build();
        when(mediaService.storeSingle(any(), eq(MediaKind.VIDEO))).thenReturn(videoAsset);
        when(mediaService.storeSingle(any(), eq(MediaKind.PREVIEW))).thenReturn(previewAsset);

        MockMultipartFile videoFile = new MockMultipartFile("video", "video.mp4", "video/mp4", "data".getBytes());
        MockMultipartFile previewFile = new MockMultipartFile("preview", "preview.png", "image/png", "data".getBytes());

        VideoWithPreviewUploadResponse response = videoService.uploadVideoWithPreview("title", "desc", VideoVisibility.PUBLIC, 1L, videoFile, previewFile);

        assertEquals(11L, response.videoId());
        assertEquals(20L, response.videoAssetId());
        verify(mediaService).createBundleItem(10L, BundleItemRole.VIDEO, 20L);
        verify(mediaService).createBundleItem(10L, BundleItemRole.PREVIEW, 30L);
    }

    @Test
    void deleteVideoWithPreview_notOwner_throws() {
        Video video = Video.builder().id(1L).ownerUserId(2L).build();
        when(videoRepository.findById(1L)).thenReturn(Optional.of(video));

        assertThatThrownBy(() -> videoService.deleteVideoWithPreview(1L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not allowed");
    }
}

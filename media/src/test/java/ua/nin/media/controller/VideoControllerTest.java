package ua.nin.media.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ua.nin.media.dto.VideoWithPreviewUploadResponse;
import ua.nin.media.service.VideoService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VideoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VideoService videoService;

    @InjectMocks
    private VideoController videoController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(videoController).build();
    }

    @Test
    void uploadVideoWithPreview_returnsResponse() throws Exception {
        VideoWithPreviewUploadResponse response = VideoWithPreviewUploadResponse.builder()
                .videoId(1L)
                .bundleId(2L)
                .videoAssetId(3L)
                .previewAssetId(4L)
                .build();
        when(videoService.uploadVideoWithPreview(any(), any(), any(), any(Long.class), any(), any())).thenReturn(response);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("1");

        MockMultipartFile title = new MockMultipartFile("title", "", "text/plain", "title".getBytes());
        MockMultipartFile video = new MockMultipartFile("video", "video.mp4", "video/mp4", "data".getBytes());
        MockMultipartFile preview = new MockMultipartFile("preview", "preview.png", "image/png", "data".getBytes());

        mockMvc.perform(multipart("/api/v1/video/upload/video-with-preview")
                        .file(title)
                        .file(video)
                        .file(preview)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.videoId").value(1));
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("1");

        mockMvc.perform(delete("/api/v1/video/1")
                        .principal(authentication))
                .andExpect(status().isNoContent());
    }
}

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
import ua.nin.media.dto.MediaMetaResponse;
import ua.nin.media.dto.MediaUploadResponse;
import ua.nin.media.model.MediaAsset;
import ua.nin.media.model.MediaKind;
import ua.nin.media.service.MediaService;

import java.io.ByteArrayInputStream;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private MediaController mediaController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(mediaController).build();
    }

    @Test
    void upload_returnsResponse() throws Exception {
        when(mediaService.uploadAsset(any())).thenReturn(new MediaUploadResponse(1L));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("1");

        MockMultipartFile file = new MockMultipartFile("file", "file.png", "image/png", "data".getBytes());

        mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(file)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaId").value(1));
    }

    @Test
    void meta_returnsResponse() throws Exception {
        MediaMetaResponse response = new MediaMetaResponse(1L, MediaKind.IMAGE, "image/png", "file.png", 12L, "hash", Instant.now(), null);
        when(mediaService.metaInformation(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/media/1/meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void download_returnsStream() throws Exception {
        MediaAsset asset = MediaAsset.builder()
                .id(1L)
                .contentType("image/png")
                .originalFilename("file.png")
                .sizeBytes(4L)
                .build();
        when(mediaService.getAssetOrThrow(1L)).thenReturn(asset);
        when(mediaService.open(1L)).thenReturn(new ByteArrayInputStream("data".getBytes()));

        mockMvc.perform(get("/api/v1/media/1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"));
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/media/1"))
                .andExpect(status().isNoContent());
    }
}

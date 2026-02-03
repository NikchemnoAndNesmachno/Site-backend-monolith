package ua.nin.media.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ua.nin.media.dto.MediaMetaResponse;
import ua.nin.media.mapper.MediaMetaResponseMapper;
import ua.nin.media.model.MediaAsset;
import ua.nin.media.model.MediaKind;
import ua.nin.media.repository.MediaAssetRepository;
import ua.nin.media.repository.MediaBundleItemRepository;
import ua.nin.media.repository.MediaBundleRepository;
import ua.nin.media.storage.MediaStorage;
import ua.nin.media.validator.MultipartMediaValidator;

import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaAssetRepository assetRepository;
    @Mock
    private MediaBundleRepository bundleRepository;
    @Mock
    private MediaBundleItemRepository bundleItemRepository;
    @Mock
    private MediaStorage storage;
    @Mock
    private MediaMetaResponseMapper mediaMetaResponseMapper;
    @Mock
    private MultipartMediaValidator multipartMediaValidator;

    @InjectMocks
    private MediaService mediaService;

    @Test
    void metaInformation_missing_throws() {
        when(assetRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mediaService.metaInformation(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Media not found");
    }

    @Test
    void deleteAsset_marksDeletedAndSwallowsStorageError() throws Exception {
        MediaAsset asset = MediaAsset.builder().id(1L).storageKey("key").build();
        when(assetRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(asset));
        doThrow(new RuntimeException("boom")).when(storage).delete("key");

        mediaService.deleteAsset(1L);

        assertNotNull(asset.getDeletedAt());
        verify(assetRepository).save(asset);
    }

    @Test
    void open_missingStorage_throws() throws Exception {
        MediaAsset asset = MediaAsset.builder().id(1L).storageKey("key").build();
        when(assetRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(asset));
        when(storage.open("key")).thenThrow(new RuntimeException("missing"));

        assertThatThrownBy(() -> mediaService.open(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("File not found");
    }

    @Test
    void storeSingle_returnsExistingWhenDuplicate() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "file.png", "image/png", "data".getBytes());
        MediaAsset existing = MediaAsset.builder().id(5L).build();
        when(assetRepository.findBySha256AndSizeBytesAndDeletedAtIsNull(anyString(), eq(file.getSize())))
                .thenReturn(Optional.of(existing));

        MediaAsset result = mediaService.storeSingle(file, MediaKind.IMAGE);

        assertEquals(5L, result.getId());
        verify(storage).save(startsWith("tmp/media/"), any(InputStream.class));
        verify(storage).delete(startsWith("tmp/media/"));
        verify(storage, never()).move(anyString(), anyString());
    }

    @Test
    void storeSingle_savesNewAsset() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "file.png", "image/png", "data".getBytes());
        when(assetRepository.findBySha256AndSizeBytesAndDeletedAtIsNull(anyString(), eq(file.getSize())))
                .thenReturn(Optional.empty());
        when(assetRepository.save(any(MediaAsset.class))).thenAnswer(inv -> inv.getArgument(0));

        MediaAsset result = mediaService.storeSingle(file, MediaKind.IMAGE);

        assertEquals(MediaKind.IMAGE, result.getKind());
        verify(storage).move(startsWith("tmp/media/"), startsWith("media/"));
    }

    @Test
    void metaInformation_mapsAsset() {
        MediaAsset asset = MediaAsset.builder().id(1L).build();
        MediaMetaResponse response = new MediaMetaResponse(1L, null, "image/png", "file.png", 12L, "hash", Instant.now(), null);
        when(assetRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(asset));
        when(mediaMetaResponseMapper.toDto(asset)).thenReturn(response);

        MediaMetaResponse result = mediaService.metaInformation(1L);

        assertEquals("image/png", result.contentType());
    }
}

package ua.nin.media.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ua.nin.media.dto.MediaMetaResponse;
import ua.nin.media.model.MediaAsset;
import ua.nin.media.model.MediaKind;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MediaMetaResponseMapperTest {

    private final MediaMetaResponseMapper mapper = Mappers.getMapper(MediaMetaResponseMapper.class);

    @Test
    void mapsMediaAssetToDto() {
        MediaAsset asset = MediaAsset.builder()
                .id(1L)
                .kind(MediaKind.IMAGE)
                .contentType("image/png")
                .originalFilename("file.png")
                .sizeBytes(12L)
                .sha256("hash")
                .build();

        MediaMetaResponse response = mapper.toDto(asset);

        assertEquals(1L, response.id());
        assertEquals(MediaKind.IMAGE, response.kind());
        assertEquals("image/png", response.contentType());
        assertEquals("file.png", response.originalFilename());
        assertEquals(12L, response.sizeBytes());
        assertEquals("hash", response.sha256());
    }
}

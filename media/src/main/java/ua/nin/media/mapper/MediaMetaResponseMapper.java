package ua.nin.media.mapper;

import org.mapstruct.Mapper;
import ua.nin.media.dto.MediaMetaResponse;
import ua.nin.media.model.MediaAsset;

@Mapper(componentModel = "spring")
public interface MediaMetaResponseMapper {
    MediaMetaResponse toDto(MediaAsset mediaAsset);
}

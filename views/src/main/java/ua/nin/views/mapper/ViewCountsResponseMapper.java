package ua.nin.views.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ua.nin.views.dto.ViewCountsResponse;
import ua.nin.views.model.ViewCount;

@Mapper(componentModel = "spring")
public interface ViewCountsResponseMapper {

    @Mapping(source = "id.targetType", target = "targetType")
    @Mapping(source = "id.targetId", target = "targetId")
    ViewCountsResponse toDto(ViewCount viewCount);

}
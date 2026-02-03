package ua.nin.views.mapper;

import org.mapstruct.Mapper;
import ua.nin.views.dto.ViewCountsResponse;
import ua.nin.views.model.ViewCount;

@Mapper(componentModel = "spring")
public interface ViewCountsResponseMapper {

    ViewCountsResponse toDto(ViewCount viewCount);

}
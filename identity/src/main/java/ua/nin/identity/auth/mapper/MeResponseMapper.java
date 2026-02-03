package ua.nin.identity.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ua.nin.identity.auth.dto.MeResponse;
import ua.nin.identity.auth.model.User;

@Mapper(componentModel = "spring")
public interface MeResponseMapper {
    @Mapping(source = "id", target = "userId")
    MeResponse toDto(User user);
}

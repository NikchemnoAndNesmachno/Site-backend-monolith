package ua.nin.identity.profile.mapper;

import org.mapstruct.Mapper;
import ua.nin.identity.profile.dto.PublicProfileResponse;
import ua.nin.identity.profile.model.Profile;

@Mapper(componentModel = "spring")
public interface PublicProfileResponseMapper {
    PublicProfileResponse toDto(Profile profile);
}
